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

package org.infogrid.mesh.set;

import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.ArrayHelper;

/**
 * Factors out common behaviors of many MeshObjectSetFactories.
 */
public abstract class AbstractMeshObjectSetFactory
        implements
            MeshObjectSetFactory
{
    /**
     * Constructor for subclasses only.
     * 
     * @param mb the MeshBase to which this MeshObjectSetFactory belongs
     */
    protected AbstractMeshObjectSetFactory()
    {
        theMeshBase = null; // not initialized
    }
    
    /**
     * Set the MeshBase on whose behalf this factory works.
     * 
     * @param newValue the new value
     */
    public void setMeshBase(
            MeshBase newValue )
    {
        if( theMeshBase != null ) {
            throw new IllegalStateException( "Already have MeshBase, cannot reset after it has been set" );
        }
        theMeshBase = newValue;
    }

    /**
     * Factory method to construct a MeshObjectSet with the single specified member.
     * 
     * @param member the content of the set
     * @return the created MeshObjectSet
     */
    public ImmutableMeshObjectSet createSingleMemberImmutableMeshObjectSet(
            MeshObject member )
    {
        return createImmutableMeshObjectSet( new MeshObject[] { member }, null );
    }

    /**
     * Factory method to construct a MeshObjectSet with the specified members.
     * 
     * @param members the content of the set
     * @return the created MeshObjectSet
     */
    public ImmutableMeshObjectSet createImmutableMeshObjectSet(
            MeshObject [] members )
    {
        return createImmutableMeshObjectSet( members, null );
    }

    /**
     * Factory method to construct a MeshObjectSet with the members of another
     * MeshObjectSet, as long as they are selected by the MeshObjectSelector.
     * 
     * @param input the input MeshObjectSet
     * @param selector determines which candidates are included
     * @return the created MeshObjectSet
     */
    public ImmutableMeshObjectSet createImmutableMeshObjectSet(
            MeshObjectSet      input,
            MeshObjectSelector selector )
    {
        MeshObject [] inputContent = input.getMeshObjects();
        MeshObject [] content;
        if( selector != null ) {
            content = new MeshObject[ inputContent.length ];
            int count = 0;
            for( int i=0 ; i<inputContent.length ; ++i ) {
                if( selector.accepts( inputContent[i] )) {
                    content[ count++ ] = inputContent[i];
                }
            }
            if( count < content.length ) {
                content = ArrayHelper.copyIntoNewArray( content, 0, count, MeshObject.class );
            }
        } else {
            content = inputContent;
        }

        ImmutableMeshObjectSet ret = createImmutableMeshObjectSet( content );
        return ret;
    }

    /**
     * Factory method to construct a MeshObjectSet with all the members of the provided
     * MeshObjectSets.
     * 
     * @param operands the sets to unify
     * @return the created MeshObjectSet
     */
    public CompositeImmutableMeshObjectSet createImmutableMeshObjectSetUnification(
            MeshObjectSet [] operands )
    {
        return createImmutableMeshObjectSetUnification( operands, null );
    }

    /**
     * Convenience factory method to construct a unification of two MeshObjectSets.
     * 
     * @param one the first set to unify
     * @param two the second set to unify
     * @return the created MeshObjectSet
     */
    public CompositeImmutableMeshObjectSet createImmutableMeshObjectSetUnification(
            MeshObjectSet one,
            MeshObjectSet two )
    {
        return createImmutableMeshObjectSetUnification( new MeshObjectSet[] { one, two } );
    }

    /**
     * Convenience factory method to construct a unification of a MeshObjectSet and
     * a second single-element MeshObjectSet.
     * 
     * @param one the first set to unify
     * @param two the second set to unify
     * @return the created MeshObjectSet
     */
    public CompositeImmutableMeshObjectSet createImmutableMeshObjectSetUnification(
            MeshObjectSet one,
            MeshObject    two )
    {
        // FIXME this can be optimized I guess
        return createImmutableMeshObjectSetUnification( new MeshObjectSet[] { one, createSingleMemberImmutableMeshObjectSet( two ) } );
    }

    /**
     * Factory method to construct a MeshObjectSet that contains those MeshObjects that are contained
     * in all of the provided MeshObjectSets.
     * 
     * @param operands the sets to unify
     * @return the created MeshObjectSet
     */
    public CompositeImmutableMeshObjectSet createImmutableMeshObjectSetIntersection(
            MeshObjectSet [] operands )
    {
        return createImmutableMeshObjectSetIntersection( operands, null );
    }

    /**
     * Convenience factory method to construct an intersection of two MeshObjectSets.
     * 
     * @param one the first set to unify
     * @param two the second set to unify
     * @return the created MeshObjectSet
     */
    public CompositeImmutableMeshObjectSet createImmutableMeshObjectSetIntersection(
            MeshObjectSet one,
            MeshObjectSet two )
    {
        return createImmutableMeshObjectSetIntersection( new MeshObjectSet[] { one, two } );        
    }

    /**
     * Factory method to construct a MeshObjectSet that contains those MeshObjects from
     * a first MeshObjectSet that are not contained in a second MeshObjectSet.
     * 
     * @param one the first MeshObjectSet
     * @param two the second MeshObjectSet
     */
    public CompositeImmutableMeshObjectSet createImmutableMeshObjectSetMinus(
            MeshObjectSet one,
            MeshObjectSet two )
    {
        return createImmutableMeshObjectSetMinus( one, two, null );
    }

    /**
     * Factory method to create an OrderedMeshObjectSet.
     * 
     * @param content the content of the OrderedMeshObjectSet
     * @param sorter the MeshObjectSorter that determines the ordering within the OrderedMeshObjectSet
     */
    public OrderedImmutableMeshObjectSet createOrderedImmutableMeshObjectSet(
            MeshObjectSet    content,
            MeshObjectSorter sorter )
    {
        return createOrderedImmutableMeshObjectSet( content, sorter, OrderedMeshObjectSet.UNLIMITED );
    }

    /**
     * Obtain the MeshBase on which this MeshObjectFactory operates.
     * 
     * @return the MeshBase
     */
    public MeshBase getMeshBase()
    {
        return theMeshBase;
    }
    
    /**
     * Helper method for implementation and subclasses: create array with the merged
     * content contained in all the sets and without duplicates.
     *
     * @param inputSets the set of input MeshObjectSets to be unified
     * @param optional selector for the resulting MeshObjects
     * @return the MeshObjects that are contained in the inputSets, but without duplicates
     */
    protected static MeshObject [] unify(
            MeshObjectSet []   inputSets,
            MeshObjectSelector selector )
    {
        int count = 0;
        for( int i=0 ; i<inputSets.length ; ++i ) {
            count += inputSets[i].size();
        }

        MeshObject [] objs = new MeshObject[ count ];

        count = 0;
        for( int i=0 ; i<inputSets.length ; ++i ) {
            MeshObject [] candidates = inputSets[i].getMeshObjects();

            for( int j=0 ; j<candidates.length ; ++j ) {
                if( !ArrayHelper.isIn( candidates[j], objs, 0, count, false )) {
                    if( selector == null || selector.accepts( candidates[j] )) {
                        objs[ count++ ] = candidates[j];
                    }
                }
            }
        }
        if( count < objs.length ) {
            objs = ArrayHelper.copyIntoNewArray( objs, 0, count, MeshObject.class );
        }

        return objs;
    }

    /**
     * Helper method for implementation and subclasses: create array with the content
     * contained in everyone of the sets and without duplicates.
     *
     * @param inputSets the set of input MeshObjectSets to be intersected
     * @param optional selector for the resulting MeshObjects
     * @return the MeshObjects that are contained in all of the inputSets
     */
    protected static MeshObject [] intersect(
            MeshObjectSet []   inputSets,
            MeshObjectSelector selector )
    {
        MeshObject [] ret = ArrayHelper.copyIntoNewArray(
                inputSets[0].getMeshObjects(),
                MeshObject.class ); // shorten later, this is max

        int takenOut = 0;
        for( int i=1; i<inputSets.length ; ++i ) {
            MeshObject [] thisContent = inputSets[i].getMeshObjects();
            for( int j=0 ; j<ret.length ; ++j ) {
                MeshObject testObject = ret[j];
                if( testObject == null ) {
                    continue; // was removed previously
                }
                if( !ArrayHelper.isIn( testObject, thisContent, false )) {
                    if( selector == null || !selector.accepts( testObject )) { // note this is !accepts
                        ret[j] = null;
                        ++takenOut;
                    }
                }
            }
        }
        if( takenOut > 0 ) {
            MeshObject [] old = ret;

            ret = new MeshObject[ old.length - takenOut ];

            for( int i=0, j=0 ; i<old.length ; ++i ) {
                if( old[i] != null ) {
                    ret[j++] = old[i];
                }
            }
        }
        return ret;
    }

    /**
     * The MeshBase to which this MeshObjectSetFactory belongs.
     */
    protected MeshBase theMeshBase;
}
