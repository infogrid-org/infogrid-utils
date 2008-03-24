//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.probe;

import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.net.NetMeshObject;

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.IterableNetMeshBaseDifferencer;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.Proxy;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;

import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Probe.ProbeUpdateSpecification;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseEvent;
import org.infogrid.probe.shadow.ShadowMeshBaseListener;
import org.infogrid.probe.shadow.m.MStagingMeshBase;

import org.infogrid.probe.xml.DomMeshObjectSetProbe;
import org.infogrid.probe.xml.SaxMeshObjectSetProbe;
import org.infogrid.probe.xml.XmlDOMProbe;
import org.infogrid.probe.xml.XmlErrorHandler;
import org.infogrid.probe.xml.XmlProbeException;
import org.infogrid.probe.yadis.YadisServiceFactory;

import org.infogrid.util.FlexibleListenerSet;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

import org.infogrid.module.Module;
import org.infogrid.module.ModuleCapability;
import org.infogrid.module.ModuleException;
import org.infogrid.module.ModuleNotFoundException;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleResolutionException;
import org.infogrid.module.StandardModuleAdvertisement;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.lang.ref.WeakReference;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Map;

/**
 * Performs most of the work of ShadowMeshBases all the way to invoking the right Probes.
 * This is a separate class in order to make it easier to reuse behavior for multiple
 * implementations of ShadowMeshBase.
 */
public class ProbeDispatcher
{
    private static final Log log = Log.getLogInstance( ProbeDispatcher.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ProbeDispatcher(
            ShadowMeshBase        meshBase,
            ProbeDirectory        directory,
            long                  timeCreated,
            long                  timeNotNeededTillExpires,
            ModuleRegistry        registry )
    {
        theShadowMeshBase           = meshBase;
        theProbeDirectory           = directory;
        theTimeCreated              = timeCreated;
        theTimeNotNeededTillExpires = timeNotNeededTillExpires;
        theModuleRegistry           = registry;
    }
    
    /**
     * Calling this will trigger the Probe to run.
     *
     * @param startTime use this time as the start time, in System.currentTimeMillis() format
     * @param coherence the CoherenceSpecification specified by the client, if any
     * @return the computed result is the number of milliseconds until the next desired invocation, or -1 if never
     * @throws ProbeException thrown if unable to compute a result
     */
    public synchronized long doUpdateNow(
            CoherenceSpecification coherence )
        throws
            ProbeException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".doUpdateNow( " + coherence + " )" );
        }

        Throwable             problem          = null; // this helps us to know what happened in the finally clause
        boolean               updated          = true; // default is "we have been updated
        StagingMeshBase       base             = null;
        long                  ret              = -1L;
        boolean               isFirstRun       = theShadowMeshBase.size() == 0;
        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();

        theCurrentUpdate = System.currentTimeMillis();
        
        if( !isFirstRun ) {
            fireUpdateStarted();
        }

        try {
            // Determine which kind of Probe to run
            
            boolean isDirectory = false;
            boolean isStream    = false;
            // there is no "isApi" because it is the default if none of the others is set

            if( isDirectory( sourceIdentifier )) {
                isDirectory = true;

            } else if( isStreamProtocol( sourceIdentifier )) {
                isStream = true;

            } // else it is an API probe, the default


            // Pick right MeshBase -- first run is different from all others

            if( isFirstRun ) {
                base = theShadowMeshBase;
            } else {
                base = MStagingMeshBase.create( theShadowMeshBase );
            }
            if( !isFirstRun && coherence == null ) {
                // use the old coherence
                coherence = determineCoherenceFromMeshObject( theShadowMeshBase.getHomeObject() );
            }

            Transaction tx = null;
            try {
                // Run the Probe
                tx = base.createTransactionAsap();

                base.initializeHomeObject( theCurrentUpdate );
                
                if( isDirectory ) {
                    updated = handleDirectory( base, coherence );
                } else if( isStream ) {
                    updated = handleStream( base, coherence );
                } else {
                    updated = handleApi( base, coherence );
                }
                
                if( base != theShadowMeshBase ) {
                    copyProbeUpdateSpecification( theShadowMeshBase.getHomeObject(), base.getHomeObject() );
                    // otherwise we lose what was there previously
                }

            } catch( IOException ex ) {
                problem = ex;
                throw new ProbeException.IO( sourceIdentifier, ex );

            } catch( TransactionException ex ) {
                problem = ex;
                throw new ProbeException.Other( sourceIdentifier, ex ); // should never happen

            } catch( URISyntaxException ex ) {
                problem = ex;
                throw new ProbeException.SyntaxError( sourceIdentifier, ex );

            } catch( RuntimeException ex ) {
                problem = ex;
                throw new ProbeException.Other( sourceIdentifier, ex );

            } catch( ProbeException ex ) {
                problem = ex;
                throw ex;

            } finally {
                // this is a long finally block, so we enclose everything in a try/catch and put the commit into the final finally
                
                Transaction tx2 = null;
                try {
                    if( problem == null && !isFirstRun && updated ) {

                        // get all the locks back
                        theUpdateInProgress = true;
                        for( MeshObject current : theShadowMeshBase ) {
                            ((NetMeshObject)current).forceObtainLock();
                        }

                        IterableNetMeshBaseDifferencer diff = new IterableNetMeshBaseDifferencer( theShadowMeshBase );

                        if( tx2 == null ) {
                            tx2 = theShadowMeshBase.createTransactionAsap();
                        }

                        ChangeSet changeSet = diff.determineChangeSet( base );

                        updated = !changeSet.isEmpty();
                        if( updated ) {
                            diff.applyChangeSet( changeSet );
                        }
                    }
                    long tookTime = System.currentTimeMillis() - theCurrentUpdate;

                    // Deal with the update policy. We apply this to theShadow rather than base, because that way
                    // we can do it after the Differencer has run, and correctly invoke whether or not there
                    // were changes.
                    
                    MeshObject home = theShadowMeshBase.getHomeObject();
                    if( !home.isBlessedBy( ProbeSubjectArea.PROBEUPDATESPECIFICATION )) {
                        // Probe did not do it, so we do it
                        if( coherence == null ) {
                            // default
                            coherence = CoherenceSpecification.getDefault();
                        }
                        blessMeshObjectWithCoherence( coherence, home );
                    }
                        
                    ProbeUpdateSpecification spec = (ProbeUpdateSpecification) home.getTypedFacadeFor( ProbeSubjectArea.PROBEUPDATESPECIFICATION );
                    if( spec != null ) {
                        try {
                            if( base != theShadowMeshBase && tx2 == null ) {
                                tx2 = theShadowMeshBase.createTransactionAsap();
                            }
                            
                            if( problem != null ) {
                                spec.performedUnsuccessfulRun( tookTime, problem );
                            } else if( updated ) {
                                spec.performedSuccessfulRunWithChange( tookTime );
                            } else {
                                spec.performedSuccessfulRunNoChange( tookTime );
                            }

                        } catch( Throwable ex3 ) { // this should never go wrong
                            log.error( ex3 );
                        }
                        IntegerValue nextRun = null;
                        try {
                            nextRun = spec.getNextProbeRun();
                        } catch( NotPermittedException ex3 ) {
                            log.error( ex3 );
                        }
                        if( nextRun != null ) {
                            ret = nextRun.value() - System.currentTimeMillis();
                        } else {
                            ret = -1L; // never again
                        }
                    }
                    
                } catch( TransactionException ex2 ) {
                    throw new ProbeException.Other( sourceIdentifier, ex2 ); // should never happen
                    
                } finally {
                    try {
                        if( tx != null ) {
                            tx.commitTransaction();
                        }
                    } catch( Throwable ex3 ) {
                        log.error( ex3 );
                    }
                    try {
                        if( tx2 != null ) {
                            tx2.commitTransaction();
                        }
                    } catch( Throwable ex3 ) {
                        log.error( ex3 );
                    }
                    theUpdateInProgress = false;
                }
            }

        } finally {

            theLastUpdate = theCurrentUpdate;
            
            if( problem != null ) {
                // do not set theLastSuccessfulUpdate
                if( !isFirstRun ) {
                    fireUpdateFinishedUnsuccessfully( problem );
                }

            } else if( updated ) {
                theLastSuccessfulUpdate = theCurrentUpdate;
                if( !isFirstRun ) {
                    fireUpdateFinishedSuccessfully();
                }

            } else { // successful but update skipped
                theLastSuccessfulUpdate = theCurrentUpdate;
                if( !isFirstRun ) {
                    fireUpdateSkipped();
                }
            }
            if( theTimeDiscoveredNotNeeded < 0 ) {
                if( !isNeeded() ) {
                    theTimeDiscoveredNotNeeded = theCurrentUpdate;
                }
            } else {
                if( isNeeded() ) {
                    theTimeDiscoveredNotNeeded = -1L;
                }
            }
            theDelayUntilNextUpdate = ret;
        }
        return ret;
    }

    /**
     * The data source refers to a directory, we need to handle that.
     */
    protected boolean handleDirectory(
            StagingMeshBase        theStagingBase,
            CoherenceSpecification coherence )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        throw new UnsupportedOperationException(); // FIXME

//        if( theShadowUpdateCalculator == null )
//            theShadowUpdateCalculator = new AdaptivePeriodicShadowUpdateCalculator(); // FIXME locally defined parameters
//
//        try {
//            File dir = new File( theURL.toURL().getFile() );
//
//            // this will create directory object already
//            facade.getModelObjectRepository().initializeHomeObjectIfNeeded(
//                    theDirectoryType,
//                    facade.getDefaultCreatedTime(),
//                    facade.getDefaultUpdatedTime() );
//
//            File [] containedFiles = determineContainedFiles( dir );
//            for( int i=0 ; i<containedFiles.length ; ++i )
//            {
//                // This is somewhat arbitrary, but looks like it could make good sense?
//                if( ! containedFiles[i].canRead())
//                    continue;
//
//                String shortFileName = containedFiles[i].getName();
//
//                ModelObjectPath path = ModelObjectPath.fromFile( containedFiles[i] );
//
//                RootEntity newDefinitionObject = facade.createForwardReferenceFromMetaObject( theDefinitionObjectType, path );
//                RootEntity newComponentObject  = facade.createFromMetaObject( theFileType, shortFileName );
//
//                newComponentObject.setName( StringValue.create( shortFileName )); // FIXME: more
//
//                facade.createFromMetaObject( theContainsType,   "contains-"   + shortFileName, facade.getHomeObject(), newComponentObject );
//                facade.createFromMetaObject( theReferencesType, "references-" + shortFileName, newComponentObject,     newDefinitionObject );
//            }
//            return true; // assumed to always have been updated
//
//        } catch( DoNotHaveLockException ex ) {
//            throw new ProbeException.ErrorInProbe( theURLString, ex, ProbeDispatcher.class );
//        } catch( MeshObjectIdentifierNotUniqueException ex ) {
//            throw new ProbeException.ErrorInProbe( theURLString, ex, ProbeDispatcher.class );
//        } catch( RuntimeException ex ) {
//            throw new ProbeException.ErrorInProbe( theURLString, ex, ProbeDispatcher.class );
//        }
    }

    /**
     * The data source refers to a stream, we need to handle that.
     *
     * @param theShadow the ShadowMeshBase that we belong to. We pass this so we don't have to depend on the WeakReference that may change
     * @param facade the ProbeMeshBaseFacade to use for instantiation
     * @return true of the data at this data source may have been updated
     * @throws ProbeException thrown if an error occurred
     * @throws TransactionException should never be thrown, but indicates that this method can only be invoked inside a Transaction
     * @throws IOException an I/O error occurred
     */
    protected boolean handleStream(
            StagingMeshBase        base,
            CoherenceSpecification coherence )
        throws
            ProbeException,
            TransactionException,
            URISyntaxException,
            IOException
    {
        boolean               updated          = false;
        byte []               content          = null;
        String                contentAsString  = null;
        String                contentType      = null;
        String                charset          = null;
        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();
        String                protocol         = sourceIdentifier.getUrlProtocol();
        URL                   url              = sourceIdentifier.toUrl();
        String                uriString        = sourceIdentifier.getUriString();

        String yadisServicesXml  = null;
        String yadisServicesHtml = null;

        long streamDataCreated      = 0L;
        long streamDataLastModified = 0L;

        if ( "file".equals( protocol )) {
            File dataSourceFile = new File( url.getFile() );
            streamDataCreated      = dataSourceFile.lastModified(); // FIXME? No API for that ...
            streamDataLastModified = dataSourceFile.lastModified();

            content     = StreamUtils.slurp( new FileInputStream( dataSourceFile ));
            contentType = null;

        } else if( "http".equals( protocol ) || "https".equals( protocol ) ) {

            HTTP.Response httpResponse = HTTP.http_get(
                    url,
                    XRDS_MIME_TYPE + ", " + HTTP_GET_ACCEPT_HEADER );

            if( httpResponse.isSuccess() && XRDS_MIME_TYPE.equals( httpResponse.getContentType() )) {
                // found XRDS content via MIME type
                
                yadisServicesXml = httpResponse.getContentAsString();

                // now ask again, without the XRDS mime type
                httpResponse = HTTP.http_get( url, HTTP_GET_ACCEPT_HEADER );

            } else {
                String yadisUrl = httpResponse.getHttpHeaderField( "X-XRDS-Location" );
                if( yadisUrl == null ) {
                    yadisUrl = httpResponse.getHttpHeaderField( "X-YADIS-Location" );
                }
                if( yadisUrl != null ) {
                    HTTP.Response yadisResponse = HTTP.http_get( yadisUrl );
                    yadisServicesXml = yadisResponse.getContentAsString();
                }
            }
            
            streamDataCreated      = httpResponse.getLastModified(); // FIXME? No API for that ...
            streamDataLastModified = httpResponse.getLastModified();

            if( !httpResponse.isSuccess() && yadisServicesXml == null ) {
                throw new ProbeException.HttpErrorResponse( sourceIdentifier, httpResponse.getResponseCode() );
            }

            content     = httpResponse.getContent();
            contentType = httpResponse.getContentType();

        } else {
            // we always assume it has changed
            streamDataCreated      = System.currentTimeMillis(); // now
            streamDataLastModified = streamDataCreated; // same

            content     = StreamUtils.slurp( url.openStream() );
            contentType = null;
        }

        if( streamDataLastModified != theMostRecentModificationDate ) {
            updated = true;
        } else if( streamDataLastModified == 0 ) { // that seems to occur
            updated = true;
        }

        if( log.isDebugEnabled() ) {
            log.debug( this + " -- handleStream() date changed: " + updated + " ( " + theMostRecentModificationDate + ", " + streamDataLastModified + " )" );
        }

        theMostRecentModificationDate = streamDataLastModified;

        if( updated ) {
            if( content == null || content.length == 0 ) {
                throw new ProbeException.EmptyDataSource( sourceIdentifier );
            }

            if( log.isDebugEnabled() ) {
                log.debug( this + " in handleStream(): content type is " + contentType );
            }

            if( contentType == null || UNKNOWN_MIME_TYPE.equals( contentType )) {
                contentType = ProbeDispatcher.guessContentTypeFromUrl( url );

            } else if( "text/xml".equals( contentType )) {
                contentType = XML_MIME_TYPE; // makes it easier down the road

            } else if(    yadisServicesXml == null
                       && ( "text/html".equals( contentType ) || "text/xhtml".equals( contentType )) ) {
                
                if( contentAsString == null ) {
                    contentAsString = new String( content );
                }
                yadisServicesHtml = contentAsString;
            }

            if( log.isDebugEnabled() ) {
                log.debug( this + " in handleStream(): content type is " + contentType );
            }

            InputStream inStream   = new ByteArrayInputStream( content );
            Class       probeClass = null;

            try {
                if( XML_MIME_TYPE.equals( contentType )) {
                    probeClass = handleXml(
                            base,
                            coherence,
                            inStream );
                } else {
                    probeClass = handleNonXml(
                            base,
                            coherence,
                            contentType,
                            inStream );
                }
            } catch( ProbeException.DontHaveProbe ex ) {
                if( yadisServicesXml == null && yadisServicesHtml == null ) {
                    throw ex; // if we have Yadis services, don't throw this exception
                }
            }

            if( theServiceFactory == null && ( yadisServicesXml != null || yadisServicesHtml != null )) {
                theServiceFactory = new YadisServiceFactory( getDocumentBuilder() );
            }
            if( yadisServicesXml != null ) {
                theServiceFactory.addYadisServicesFromXml( sourceIdentifier, yadisServicesXml, base );
            } else if( yadisServicesHtml != null ) {
                theServiceFactory.addYadisServicesFromHtml( sourceIdentifier, yadisServicesHtml, base );
            }
            
        }
        return updated;
    }

    /**
     * This data source refers to an API, we need to handle that.
     *
     * @param theShadow the StagingMeshBase that we belong to. We pass this so we don't have to depend on the WeakReference that may change
     * @param facade the ProbeRepositoryFacade to use for instantiation
     * @return true of the data at this data source may have been updated
     * @throws ProbeException thrown if an error occurred
     * @throws TransactionException should never be thrown, but indicates that this method can only be invoked inside a Transaction
     * @throws IOException an I/O error occurred
     */
    protected boolean handleApi(
            StagingMeshBase        base,
            CoherenceSpecification coherence )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        NetMeshBaseIdentifier          sourceIdentifier = theShadowMeshBase.getIdentifier();
        ProbeDirectory.MatchDescriptor desc1            = theProbeDirectory.getApiProbeDescriptorByMatchedUrl( sourceIdentifier.getUriString() );

        Class       foundClass       = null;
        String      foundClassName   = null;
        Map         foundParameters  = null;
        ClassLoader foundClassLoader = null;

        if( desc1 != null ) {
            foundClass      = desc1.getProbeClass();
            foundClassName  = desc1.getProbeClassName();
            foundParameters = desc1.getParameters();

            if( log.isDebugEnabled() ) {
                log.debug( this + ": based on match, found name for Probe class: " + foundClassName );
            }
            
        } else {
            try {
                ProbeDirectory.ApiProbeDescriptor desc2 = theProbeDirectory.getApiProbeDescriptorByProtocol( sourceIdentifier.getUrlProtocol() );

                if( desc2 != null ) {
                    foundClass      = desc2.getProbeClass();
                    foundClassName  = desc2.getProbeClassName();
                    foundParameters = desc2.getParameters();

                    if( log.isDebugEnabled() ) {
                        log.debug( this + ": based on protocol, found name for Probe class: " + foundClassName );
                    }
                }
            } catch( MalformedURLException ex ) {
                // thrown for unknown protocols. This is fine.
            }
        }

        ApiProbe probe = null;
        if( foundClass == null && foundClassName != null ) {
            if( theModuleRegistry != null ) {
                // we take the first module that supports this interface/class
                StandardModuleAdvertisement [] candidates = theModuleRegistry.findAdvertisementsForInterface( foundClassName, Integer.MAX_VALUE );
                for( int i=0 ; i<candidates.length ; ++i ) {
                    ModuleCapability [] caps = candidates[i].findCapabilitiesByInterface( foundClassName );
                    if( caps != null && caps.length > 0 ) {
                        try {
                            Module foundModule = theModuleRegistry.resolve( candidates[i], true );
                            foundClassLoader = foundModule.getClassLoader();
                            break;
                        } catch( ModuleResolutionException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( MalformedURLException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( ModuleNotFoundException ex ) {
                            log.warn( "Module not found for adv: " + candidates[i], ex );
                        }
                    }
                }
            }
            if( foundClassLoader == null ) { // attempt default loader
                foundClassLoader = getClass().getClassLoader();
            }

            try {
                foundClass = Class.forName( foundClassName, true, foundClassLoader );
            } catch( ClassNotFoundException ex ) {
                throw new ProbeException.DontHaveApiProbe( sourceIdentifier, ex );
            }
        }

        if( foundClass != null ) {
            try {
                probe = (ApiProbe) foundClass.newInstance();

            } catch( InstantiationException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( IllegalAccessException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
        }

        if( probe != null ) {
            // DifferencerMeshBase ret = null;

            if( log.isDebugEnabled() ) {
                log.debug( this + ": invoking the probe" );
            }

            ChangeSet changesToWriteBack;
            synchronized( this ) {
                changesToWriteBack    = theChangesToWriteBack;
                theChangesToWriteBack = null;
            }
            
            try {
                if( probe instanceof WriteableProbe && changesToWriteBack != null ) {
                    ((WriteableProbe) probe).write( sourceIdentifier, changesToWriteBack );
                }

                probe.readFromApi( sourceIdentifier, coherence, base );

            } catch( NotPermittedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( MeshObjectIdentifierNotUniqueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( URISyntaxException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( ModuleException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( RuntimeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
            if( log.isDebugEnabled() ) {
                log.debug( this + ": probe came back without exception" );
            }
        } else {
            throw new ProbeException.DontHaveApiProbe( sourceIdentifier, null );
        }
        return true; // we don't know, we always say we might have been updated because that's safer
    }
    
    /**
     * Do the import for an XML file.
     * 
     * @param theShadow the ShadowMeshBase that we belong to. We pass this so we don't have to depend on the WeakReference that may change
     * @param theURL the URL from which the XML file is taken
     * @param theURLString the URL in String format, for optimization reasons
     * @param inStream the stream through which we read the XML file
     * @param facade the ProbeMeshBaseFacade through which we import
     * @throws ProbeException thrown if an error occurred
     * @throws TransactionException should never be thrown, but indicates that this method can only be invoked inside a Transaction
     * @throws IOException an I/O error occurred
     */
    protected Class handleXml(
            StagingMeshBase        base,
            CoherenceSpecification coherence,
            InputStream            inStream )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".handleXml( " + base + ", " + inStream + " )" );
        }

        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();
        XmlErrorHandler       errorListener    = new XmlErrorHandler( sourceIdentifier, log );

        DocumentBuilder theDocumentBuilder = getDocumentBuilder();
        
        theDocumentBuilder.setErrorHandler( errorListener );

        Document doc;

        try {
            doc = theDocumentBuilder.parse( inStream );
        } catch( SAXException ex ) {
            throw new ProbeException.SyntaxError( sourceIdentifier, ex );
        }

        if( errorListener.numberOfErrors() > 0 ) {
            throw new ProbeException.SyntaxError( sourceIdentifier, errorListener.getAsException() );
        }

        if( !doc.hasChildNodes() ) {
            throw new ProbeException.EmptyDataSource( sourceIdentifier );
        }

        DocumentType docType = doc.getDoctype();
        String       tagType = doc.getDocumentElement().getTagName();

        if( log.isDebugEnabled() ) {
            if( docType != null ) {
                log.debug( this + ": parsed XML input, found document type \"" + docType.getName() + "\"" );
            } else {
                log.debug( this + ": parsed XML input, found null document type" );
            }
        }

        XmlDOMProbe        probe            = null;
        String             foundClassName   = null;
        Class              foundClass       = null;
        ClassLoader        foundClassLoader = null;
        Map<String,Object> foundParameters  = null;

        if( docType != null ) {
            if( SaxMeshObjectSetProbe.MESHOBJECT_SET_TAG.equalsIgnoreCase( docType.getName() )) {
                Class ret = handleNativeFormat( doc, coherence, base );
                return ret;
            }

            ProbeDirectory.XmlDomProbeDescriptor desc = theProbeDirectory.getXmlDomProbeDescriptorByDocumentType( docType.getName() );
            if( desc != null ) {
                foundClass      = desc.getProbeClass();
                foundClassName  = desc.getProbeClassName();
                foundParameters = desc.getParameters();

                if( log.isDebugEnabled() ) {
                    log.debug( this + ": based on doctype, found name for probe class: " + foundClassName );
                }
            }

        } else {
            if ( tagType != null ) {
                ProbeDirectory.XmlDomProbeDescriptor desc = theProbeDirectory.getXmlDomProbeDescriptorByTagType( tagType );
                if( desc != null ) {
                    foundClass      = desc.getProbeClass();
                    foundClassName  = desc.getProbeClassName();
                    foundParameters = desc.getParameters();

                    if( log.isDebugEnabled() ) {
                        log.debug( this + ": based on tagtype, found name for probe class: " + foundClassName );
                    }
                }
                
            } else {
                if( log.isDebugEnabled() ) {
                  log.info( this + ".handleXml - tagType is null - no probe found" );
                }

                throw new ProbeException.CannotDetermineContentType(
                        sourceIdentifier,
                        new XmlProbeException.CannotDetermineDtd( sourceIdentifier, docType, null ));
            }
        }

        if( foundClass == null && foundClassName != null ) {
            if( theModuleRegistry != null ) {
                // we take the first module that supports this interface/class
                StandardModuleAdvertisement [] candidates = theModuleRegistry.findAdvertisementsForInterface( foundClassName, Integer.MAX_VALUE );
                for( int i=0 ; i<candidates.length ; ++i ) {
                    ModuleCapability [] caps = candidates[i].findCapabilitiesByInterface( foundClassName );
                    if( caps != null && caps.length > 0 ) {
                        try {
                            Module foundModule = theModuleRegistry.resolve( candidates[i], true );
                            foundClassLoader = foundModule.getClassLoader();
                            break;
                        } catch( ModuleResolutionException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( ModuleNotFoundException ex ) {
                            log.warn( "Module not found for adv: " + candidates[i], ex );
                        }
                    }
                }
            }
            if( foundClassLoader == null ) { // attempt default loader
                foundClassLoader = getClass().getClassLoader();
            }

            try {
                foundClass = Class.forName( foundClassName, true, foundClassLoader );
            } catch( ClassNotFoundException ex ) {
                throw new ProbeException.DontHaveXmlStreamProbe( sourceIdentifier, docType != null ? docType.getName() : null, tagType, ex );
            }
        }

        if( foundClass != null ) {
            try {
                probe = (XmlDOMProbe) foundClass.newInstance();

            } catch( IllegalAccessException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( InstantiationException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
        }

        if( probe != null ) {
            if( log.isDebugEnabled() ) {
                log.debug( this + ": invoking the probe" );
            }
        
            ChangeSet changesToWriteBack;
            synchronized( this ) {
                changesToWriteBack    = theChangesToWriteBack;
                theChangesToWriteBack = null;
            }

            try {
                if( probe instanceof WriteableProbe ) {
                    ((WriteableProbe) probe).write( sourceIdentifier, changesToWriteBack );
                }

                probe.parseDocument( sourceIdentifier, coherence, doc, base );

            } catch( NotPermittedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( MeshObjectIdentifierNotUniqueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( URISyntaxException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( ModuleException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( RuntimeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
            if( log.isDebugEnabled() ) {
                log.debug( this + ": probe came back without exception" );
            }

        } else {
            throw new ProbeException.DontHaveXmlStreamProbe( sourceIdentifier, docType != null ? docType.getName() : null, tagType, null );
        }
        return foundClass;
    }

    /**
     * Do the import for a non-XML file.
     * 
     * 
     * @param theShadow the ShadowMeshBase that we belong to. We pass this so we don't have to depend on the WeakReference that may change
     * @param theURL the URL from which the stream is taken
     * @param theURLString the URL in String format, for optimization reasons
     * @param contentType the MIME type of the stream
     * @param inStream the stream through which we read the file
     * @param facade the ProbeMeshBaseFacade through which we import
     * @throws ProbeException thrown if an error occurred
     * @throws TransactionException should never be thrown, but indicates that this method can only be invoked inside a Transaction
     * @throws IOException an I/O error occurred
     */
    protected Class handleNonXml(
            StagingMeshBase        base,
            CoherenceSpecification coherence,
            String                 contentType,
            InputStream            inStream )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".handleNonXml( " + contentType + ", " + inStream + " )" );
        }

        NonXmlStreamProbe     probe            = null;
        Class                 foundClass       = null;
        String                foundClassName   = null;
        ClassLoader           foundClassLoader = null;
        Map<String,Object>    foundParameters  = null;
        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();

        ProbeDirectory.StreamProbeDescriptor desc = theProbeDirectory.getStreamProbeDescriptorByMimeType( contentType );

        if( desc == null ) {
            desc = theProbeDirectory.getDefaultStreamProbe();
        }

        if( desc != null ) {
            foundClass      = desc.getProbeClass();
            foundClassName  = desc.getProbeClassName();
            foundParameters = desc.getParameters();

            if( log.isDebugEnabled() ) {
                log.debug( this + ": based on mime type, found name for probe class: " + foundClassName );
            }
        }

        if( foundClass == null && foundClassName != null ) {
            if( theModuleRegistry != null ) {
                // we take the first module that supports this interface/class
                StandardModuleAdvertisement [] candidates = theModuleRegistry.findAdvertisementsForInterface( foundClassName, Integer.MAX_VALUE );
                for( int i=0 ; i<candidates.length ; ++i ) {
                    ModuleCapability [] caps = candidates[i].findCapabilitiesByInterface( foundClassName );
                    if( caps != null && caps.length > 0 ) {
                        try {
                            Module foundModule = theModuleRegistry.resolve( candidates[i], true );
                            foundClassLoader = foundModule.getClassLoader();
                            break;

                        } catch( ModuleResolutionException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( MalformedURLException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( ModuleNotFoundException ex ) {
                            log.warn( "Module not found for adv: " + candidates[i], ex );
                        }
                    }
                }
            }
            if( foundClassLoader == null ) { // attempt default loader
                foundClassLoader = getClass().getClassLoader();
            }

            try {
                foundClass = Class.forName( foundClassName, true, foundClassLoader );
            } catch( ClassNotFoundException ex ) {
                throw new ProbeException.DontHaveNonXmlStreamProbe( sourceIdentifier, contentType, ex );
            }
        }

        if( foundClass != null ) {
            try {
                probe = (NonXmlStreamProbe) foundClass.newInstance();
            } catch( InstantiationException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( IllegalAccessException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
        }
        if( probe != null ) {
            // DifferencerMeshBase ret = null;

            if( log.isDebugEnabled() ) {
                log.debug( this + ": invoking the probe" );
            }

            ChangeSet changesToWriteBack;
            synchronized( this ) {
                changesToWriteBack    = theChangesToWriteBack;
                theChangesToWriteBack = null;
            }

            try {
                if( probe instanceof WriteableProbe ) {
                    ((WriteableProbe) probe).write( sourceIdentifier, changesToWriteBack );
                }

                probe.readFromStream( sourceIdentifier, coherence, inStream, contentType, base );

            } catch( NotPermittedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( MeshObjectIdentifierNotUniqueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( URISyntaxException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( ModuleException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( RuntimeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
            if( log.isDebugEnabled() ) {
                log.debug( this + ": probe came back without exception" );
            }
        } else {
            throw new ProbeException.DontHaveNonXmlStreamProbe( sourceIdentifier, contentType, null );
        }
        return foundClass;
    }

    /**
     * This handles the case of the native InfoGrid XML format.
     * 
     * @param theURL the URL from which the XML file is taken
     * @param doc the DOM of the XML file
     * @param theRepository the ModelObjectRepository into which we import. The native format does not use a facade.
     * @throws ProbeException thrown if an error occurred
     * @throws TransactionException should never be thrown, but indicates that this method can only be invoked inside a Transaction
     * @throws IOException an I/O error occurred
     */
    protected Class handleNativeFormat(
            Document               doc,
            CoherenceSpecification coherence,
            StagingMeshBase        base )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".handleNativeFormat( " + doc + " )" );
        }

        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();
        DomMeshObjectSetProbe theProbe         = new DomMeshObjectSetProbe();

        try {
            theProbe.parseDocument( sourceIdentifier, coherence, doc, base );

            return theProbe.getClass();

        } catch( NotPermittedException ex ) {
            throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, ProbeDispatcher.class );
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, ProbeDispatcher.class );
        }
    }
    
    /**
     * Helper method to determine a CoherenceSpecification from a MeshObject, if it is suitably blessed.
     *
     * @param obj the MeshObject
     * @return the found CoherenceSpecification, or null
     */
    protected CoherenceSpecification determineCoherenceFromMeshObject(
            MeshObject obj )
    {
        CoherenceSpecification ret;
        EntityType subtype = obj.determineBlessedSubtype( ProbeSubjectArea.PROBEUPDATESPECIFICATION );

        try {
            if( subtype == null ) {
                ret = null;

            } else if( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION == subtype ) {

                long   fallbackDelay  = ((IntegerValue)obj.getPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_FALLBACKDELAY  )).value();
                long   maxDelay       = ((IntegerValue)obj.getPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_MAXDELAY       )).value();
                double adaptiveFactor = ((FloatValue)  obj.getPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_ADAPTIVEFACTOR )).value();

                ret = new CoherenceSpecification.AdaptivePeriodic(
                        fallbackDelay,
                        maxDelay,
                        adaptiveFactor );

            } else if( ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION == subtype ) {

                ret = CoherenceSpecification.ONE_TIME_ONLY;

            } else if( ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION == subtype ) {

                long period = ((IntegerValue)obj.getPropertyValue( ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION_DELAY )).value();

                ret = new CoherenceSpecification.Periodic( period );

            } else {

                log.error( "ProbeUpdateSpecification subtype not supported: " + subtype.getIdentifier().toExternalForm() );
                ret = null;

            }

        } catch( NotPermittedException ex ) {
            log.error( ex );
            ret = null;
        }
        return ret;
    }
    
    /**
     * Helper method to bless a MeshObject with a suitable MeshType, given the provided CoherenceSpecification.
     *
     * @param coherence the CoherenceSpecification
     * @param obj the MeshObject
     */
    protected void blessMeshObjectWithCoherence(
            CoherenceSpecification coherence,
            MeshObject             obj )
        throws
            TransactionException
    {
        try {
            if( coherence == CoherenceSpecification.ONE_TIME_ONLY ) {

                obj.bless( ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION );

            } else if( coherence instanceof CoherenceSpecification.Periodic ) {

                CoherenceSpecification.Periodic realCoherence = (CoherenceSpecification.Periodic) coherence;

                obj.bless( ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION );
                obj.setPropertyValue( ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION_DELAY, IntegerValue.create( realCoherence.getPeriod() ));

            } else if( coherence instanceof CoherenceSpecification.AdaptivePeriodic ) {

                CoherenceSpecification.AdaptivePeriodic realCoherence = (CoherenceSpecification.AdaptivePeriodic) coherence;

                obj.bless( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION );
                obj.setPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_FALLBACKDELAY,  IntegerValue.create( realCoherence.getFallbackDelay() ));
                obj.setPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_MAXDELAY,       IntegerValue.create( realCoherence.getMaxDelay() ));
                obj.setPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_ADAPTIVEFACTOR, FloatValue.create(   realCoherence.getAdaptiveFactor() ));

            } else {

                log.error( "CoherenceSpecification subtype not supported: " + coherence );
            }
        } catch( NotPermittedException ex ) {
            log.error( ex );
        }
    }

    /**
     * Copy the ProbeUpdateSpecification from one MeshObject to another.
     *
     * @param source the source MeshObject
     * @param destination the destination MeshObject
     */
    protected void copyProbeUpdateSpecification(
            MeshObject source,
            MeshObject destination )
        throws
            TransactionException
    {
        try {
            EntityType sourceSubtype = source.determineBlessedSubtype( ProbeSubjectArea.PROBEUPDATESPECIFICATION );
            if( sourceSubtype == null ) {
                return;
            }

            EntityType destinationSubtype = destination.determineBlessedSubtype( ProbeSubjectArea.PROBEUPDATESPECIFICATION );
            if( destinationSubtype != null ) {
                return;
            }

            // copy over
            destination.bless( sourceSubtype );
            for( PropertyType prop : sourceSubtype.getAllPropertyTypes() ) {
                if( prop.getIsReadOnly().value() ) {
                    continue;
                }
                PropertyValue value = source.getPropertyValue( prop );
                destination.setPropertyValue( prop, value );
            }
        } catch( NotPermittedException ex ) {
            log.error( ex );
        }
    }

    /**
     * Obtain the time at which this ShadowMeshBase was created.
     *
     * @return the time at which this ShadowMeshBase was created, in System.currentTimeMillis() format
     */
    public long getTimeCreated()
    {
        return theTimeCreated;
    }

    /**
     * Obtain the time at which the most recent successful update of this
     * ShadowMeshBase was started.
     *
     * @return the time at which the update started, in System.currentTimeMillis() format
     */
    public long getLastSuccessfulUpdateStartedTime()
    {
        return theLastSuccessfulUpdate;
    }

    /**
     * Obtain the time at which the most recent update of this ShadowMeshBase
     * was started, regardless of whether it was successful or not. This is only
     * updated once the update has finished.
     *
     * @return the time at which the update started, in System.currentTimeMillis() format
     * @see getCurrentUpdateStartedTime
     */
    public long getLastUpdateStartedTime()
    {
        return theLastUpdate;
    }

    /**
     * Obtain the time at which the current run was started. This is updated as soon
     * as the run starts.
     *
     * @return the start time of the current update
     */
    public long getCurrentUpdateStartedTime()
    {
        return theCurrentUpdate;
    }

    /**
     * Obtain the number of milliseconds from now until the next update is supposed to happen.
     *
     * @return the number of milliseconds from now until the next update is supposed to happen, or 0 if as soon as possible, or -1 if none.
     */
    public long getDelayUntilNextUpdate()
    {
        if( theDelayUntilNextUpdate == MAGIC_UNINITIALIZED_DELAY_UNTIL_NEXT_UPDATE ) {
            // need to initialize first

            theDelayUntilNextUpdate = 0L; // now if anything goes wrong
            MeshObject home = theShadowMeshBase.getHomeObject();
            try {
                IntegerValue found = (IntegerValue) home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_NEXTPROBERUN );
                if( found == null ) {
                    return -1L;
                }
                theDelayUntilNextUpdate = found.value() - System.currentTimeMillis();
                if( theDelayUntilNextUpdate < 0L ) {
                    theDelayUntilNextUpdate = 0; // as soon as possible
                }

            } catch( NotPermittedException ex ) {
                log.error( ex );
            } catch( EntityNotBlessedException ex ) {
                log.error( ex );
            }
        }
        return theDelayUntilNextUpdate;
    }

    /**
     * Determine whether this ShadowMeshBase is still needed. It is needed if at least
     * one its MeshObjects is replicated to/from another NetMeshBase.
     *
     * @return true if it is still needed
     */
    public synchronized boolean isNeeded()
    {
        for( MeshObject current : theShadowMeshBase ) {
            Proxy [] proxies = ((NetMeshObject)current).getAllProxies();
            if( proxies != null && proxies.length > 0 ) {
                return true; // don't need to do anything
            }
        }
        return false;
    }
    
    /**
     * Determine whether this ShadowMeshBase can be deleted, because it is not needed
     * any more, and the necessary timeNotNeededTillExpires has expired.
     *
     * @return true if it can be deleted
     */
    public boolean mayBeDeleted()
    {
        if( theTimeDiscoveredNotNeeded < 0 ) {
            return false;
        }
        if( theTimeNotNeededTillExpires < 0 ) {
            return false;
        }
        long delta = System.currentTimeMillis() - theTimeDiscoveredNotNeeded;
        if( delta < theTimeNotNeededTillExpires ) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether an update is in progress.
     *
     * @return true if an update is currently in progress on another thread
     */
    public boolean getIsUpdateInProgress()
    {
        return theUpdateInProgress;
    }
    
    /**
     * Obtain the current problem with updating this ShadowMeshBase, if any. This
     * is a "bound" property.
     *
     * @return the current problem with updating this ShadowMeshBase
     */
    public Throwable getCurrentProblem()
    {
        return theCurrentProblem;
    }

    /**
     * Queue new changes for the Shadow. These changes will be written out by the Probe
     * prior to reading the data source again.
     *
     * @param newChangeSet the set of new Changes
     */
    public synchronized void queueNewChanges(
            ChangeSet newChangeSet )
    {
        if( theChangesToWriteBack == null ) {
            theChangesToWriteBack = ChangeSet.create( newChangeSet );
        } else {
            theChangesToWriteBack.append( newChangeSet );
        }
    }
    
    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #removeShadowListener
     */
    public void addDirectShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theShadowListeners.addDirect( newListener );
    }

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #removeShadowListener
     */
    public void addWeakShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theShadowListeners.addWeak( newListener );
    }

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #removeShadowListener
     */
    public void addSoftShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theShadowListeners.addSoft( newListener );
    }

    /**
     * Remove a listener to listen to ShadowMeshBase-specific events.
     *
     * @param oldListener the listener to be removed
     * @see #addShadowListener
     */
    public void removeShadowListener(
            ShadowMeshBaseListener oldListener )
    {
        theShadowListeners.remove( oldListener );
    }

    /**
     * Obtain a DocumentBuilder
     */
    protected synchronized DocumentBuilder getDocumentBuilder()
    {
        DocumentBuilder ret = null;
        
        if( theDocumentBuilderRef != null ) {
            ret = theDocumentBuilderRef.get();
        }
        if( ret == null ) {
            // We do this messing thing with the context ClassLoader because otherwise,
            // there is no chance that our XML parser can be found. The Sun XML classloader
            // finder is an awful implementation using a broken conceptual model how things
            // should work, but at least we found this workaround.

            ClassLoader ctxt = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

                // do XML initialization in the constructor
                DocumentBuilderFactory theDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
                theDocumentBuilderFactory.setNamespaceAware( true );

                // FIXME? theDocumentBuilderFactory.setValidating( true );
                theDocumentBuilderFactory.setIgnoringComments( true );
                theDocumentBuilderFactory.setIgnoringElementContentWhitespace( true );

                ret = theDocumentBuilderFactory.newDocumentBuilder();
            } catch( Exception ex ) {
               log.error( ex );
               log.error( "DocumentBuilderFactory has class loader " + DocumentBuilderFactory.class.getClassLoader() );
            } finally {
                Thread.currentThread().setContextClassLoader( ctxt );
            }
            theDocumentBuilderRef = new WeakReference<DocumentBuilder>( ret );
        }
        return ret;
    }

    /**
     * Helper method to guess a MIME content type from a URL. We leverage the JDK.
     * Basically we look at the extension.
     * FIXME: this should not be hardcoded.
     *
     * @param u the URL whose MIME type shall be guessed
     * @return the MIME type, or null
     */
    public static String guessContentTypeFromUrl(
            URL u )
    {
        String fileName = u.getFile();

        String type = URLConnection.getFileNameMap().getContentTypeFor( fileName );
        if( type != null ) {
            return type;
        }

        // If the system does not know it, we add our own table here
        if( !"file".equals( u.getProtocol() )) {
            return UNKNOWN_MIME_TYPE;
        }

        int period = fileName.lastIndexOf( '.' );
        if( period < 0 ) {
            return UNKNOWN_MIME_TYPE;
        }

        String extension = fileName.substring( period + 1 );
        if( "vcf".equals( extension )) {
            return "text/x-vcard";
        }

        return UNKNOWN_MIME_TYPE;
    }

    /**
     * Determine whether a given NetMeshBaseIdentifier refers to a directory or not.
     * 
     * @param proto the protocol to test
     * @param filePartOfUrl the file component of the URL (the file system path in case of a "file" URL)
     * @return true if this references a directory that exists
     */
    protected static boolean isDirectory(
            NetMeshBaseIdentifier id )
    {
        try {
            URL url = id.toUrl();

            if( !"file".equals( url.getProtocol() )) {
                return false;
            }
            File f = new File( url.getPath() );
            if( !f.exists() ) {
                return false;
            }
            if( !f.isDirectory() ) {
                return false;
            }
            return true;
            
        } catch( MalformedURLException ex ) {
            return false;
        } catch( IllegalArgumentException ex ) { // thrown if the NetMeshBaseIdentifier does not have a protocol, for example
            return false;
        }
    }

    /**
     * Determine whether a given protocol is a protocol that requires us to read a
     * stream, or it is a non-stream protocol.
     *
     * @param proto the protocol candidate
     * @return true if this protocol requires us to read a stream
     */
    protected static boolean isStreamProtocol(
            NetMeshBaseIdentifier id )
    {
        try {
            String proto = id.getUrlProtocol();

           return "http".equals( proto ) || "https".equals( proto ) ||"ftp".equals( proto ) || "file".equals( proto );
           
        } catch( MalformedURLException ex ) {
            return false;
        } catch( IllegalArgumentException ex ) {
            return false;
        }
    }

    /**
     * Fire a "we are about to start updating" event.
     */
    protected void fireUpdateStarted()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".fireUpdateStarted()" );
        }

        theShadowListeners.fireEvent( new ShadowMeshBaseEvent( theShadowMeshBase ), 0 );
    }

    /**
     * Fire a "we decided against updating" event.
     */
    protected void fireUpdateSkipped()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".fireUpdateSkipped()" );
        }

        theShadowListeners.fireEvent( new ShadowMeshBaseEvent( theShadowMeshBase ), 1 );
    }

    /**
     * Fire a "we are finished updating and we were successful" event.
     */
    protected void fireUpdateFinishedSuccessfully()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".fireUpdateFinishedSuccessfully()" );
        }

        theShadowListeners.fireEvent( new ShadowMeshBaseEvent( theShadowMeshBase ), 2 );        
    }

    /**
     * Fire a "we are finished updating but we were unsuccessful" event.
     *
     * @param problem the Throwable indicating the problem that occurred
     */
    public void fireUpdateFinishedUnsuccessfully(
             Throwable problem )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".fireUpdateFinishedUnsuccessfully( " + problem + " )" );
        }

        theShadowListeners.fireEvent( new ShadowMeshBaseEvent( theShadowMeshBase ), 3 );        
    }

    /**
     * The ShadowMeshBase on whose behalf we work.
     */
    protected ShadowMeshBase theShadowMeshBase;

    /**
     * The time at which this shadow was created.
     */
    protected long theTimeCreated;
    
    /**
     * The time at which the last successful update of this shadow was started.
     * This is in System.currentTimeMillis() format.
     */
    protected long theLastSuccessfulUpdate;

    /**
     * The time at which the last update of this shadow was started, whether successful or not.
     * This is in System.currentTimeMillis() format.
     */
    protected long theLastUpdate;

    /**
     * The time at which the current update of this shadow was started.
     * This is in System.currentTimeMillis() format.
     */
    protected long theCurrentUpdate;
    
    /**
     * The time at which the next update is supposed to start, or -1 if none.
     * This is in System.currentTimeMillis() format.
     */
    protected long theDelayUntilNextUpdate = MAGIC_UNINITIALIZED_DELAY_UNTIL_NEXT_UPDATE;
    
    /**
     * The last modification date of the data source in question.
     */
    private long theMostRecentModificationDate = 0;

    /**
     * The current problem with updating this shadow, if any. This is a "bound" property whose
     * changes get communicated via the ShadowMeshBaseListener mechanism.
     */
    protected Throwable theCurrentProblem;

    /**
     * The set of Changes to write to a Writeable Probe (if any).
     */
    protected ChangeSet theChangesToWriteBack = null;

    /**
      * The directory of all Probes that we know.
      */
    protected ProbeDirectory theProbeDirectory;

    /**
     * The registry of all known Modules, or null if not present.
     */
    protected ModuleRegistry theModuleRegistry;

    /**
     * The list of shadow listeners if any.
     */
    protected FlexibleListenerSet<ShadowMeshBaseListener,ShadowMeshBaseEvent,Integer> theShadowListeners
            = new FlexibleListenerSet<ShadowMeshBaseListener,ShadowMeshBaseEvent,Integer>(){
                    /**
                     * Fire the event to one contained object.
                     *
                     * @param listener the receiver of this event
                     * @param event the sent event
                     * @param parameter dispatch parameter
                     */
                    protected void fireEventToListener(
                            ShadowMeshBaseListener listener,
                            ShadowMeshBaseEvent    event,
                            Integer                parameter )
                    {
                        switch( parameter ) {
                            case 0:
                                listener.updateStarting( event );
                                break;
                            case 1:
                                listener.updateSkipped( event );
                                break;
                            case 2:
                                listener.updateFinishedSuccessfully( event );
                                break;
                            case 3:
                                listener.updateFinishedUnsuccessfully( event );
                                break;
                        }
                    }
            };

    /**
     * The DocumentBuilder for XML parsing.
     */
    protected WeakReference<DocumentBuilder> theDocumentBuilderRef;

    /**
     * Time, in System.currentTimeMillis() format, when the current (or most recent)
     * Probe run started. We need this to make sure our MeshBaseLifecycleManager uses
     * it as the default object creation time.
     */
    protected long theStartOfThisRun;
    
    /**
     * If true, an update from the ProbeDispatcher is currently in progress.
     */
    protected boolean theUpdateInProgress;
    
    /**
     * The time this ShadowMeshBase is not needed until it expires.
     */
    protected long theTimeNotNeededTillExpires;

    /**
     * The time at which it was discovered that this ShadowMeshBase was not needed. (and ever since.)
     */
    protected long theTimeDiscoveredNotNeeded = -1L;

    /**
     * The factory for YadisServices.
     */
    protected YadisServiceFactory theServiceFactory;

    /**
     * We expect this MIME type to indicate that a stream is XML.
     */
    public static final String XML_MIME_TYPE = "application/xml";

    /**
     * This MIME type indicates that a stream is unknown.
     */
    public static final String UNKNOWN_MIME_TYPE = "content/unknown";

    /**
     * This MIME type indicates a Yadis file.
     */
    public static final String XRDS_MIME_TYPE = "application/xrds+xml";
    
    /**
     * This is the default accept header for HTTP requests.
     */
    public static final String HTTP_GET_ACCEPT_HEADER = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";

    /**
     * The default time, in milliseconds, until a timeout occurs on accessing the AccessSemaphore.
     */
    public static final long DEFAULT_ACCESS_SEMAPHORE_TIMEOUT = 10000L;
    
    /**
     * Magic number indicating a non-initialized theDelayUntilNextUpdate.
     */
    private static final int MAGIC_UNINITIALIZED_DELAY_UNTIL_NEXT_UPDATE = -19191919;
}
