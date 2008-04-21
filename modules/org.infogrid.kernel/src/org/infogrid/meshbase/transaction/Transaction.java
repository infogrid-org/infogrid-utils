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

package org.infogrid.meshbase.transaction;

import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.StringHelper;

import org.infogrid.util.logging.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.infogrid.util.FlexibleListenerSet;

/**
  * <p>The concept of a Transaction in InfoGrid. This is an abstract class;
  *    specific implementations of MeshBase provide concrete subclasses.</p>
  * <p>Transactions are the bracket for a unit of Changes (called a ChangeSet) on
  *    a MeshBase.</p>
  */
public abstract class Transaction
{
    private static final Log log = Log.getLogInstance(Transaction.class); // our own, private logger

    /**
      * Private constructor, as Transactions can only be created by the MeshBase that they guard.
      * They can also only be created within the Thread by which they will be used.
      *
      * @param transactable the MeshBase that the Transaction guards
      */
    protected Transaction(
            MeshBase transactable )
    {
        theTransactable = transactable;

        myThread  = Thread.currentThread();
        myInvoker = new TransactionConstructionMarker();

        status = Status.TRANSACTION_STARTED;

        if( log.isDebugEnabled() ) {
            log.debug( "created " + this );
        }
    }

    /**
      * Commit a started Transaction. At this time, committing is the only way of
      * ending an opened Transaction; rollback is not supported (see documentation).
      */
    public synchronized void commitTransaction()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".commitTransaction()" );
        }

        try {
            checkThreadIsAllowed();
        } catch( IllegalTransactionThreadException ex ) {
            throw new IllegalStateException( "trying to commit transaction from wrong thread" );
        }

        if( !( status == Status.TRANSACTION_STARTED || status == Status.TRANSACTION_VOTED )) {
            log.error( "illegal state for transaction: " + status );
        }

        preCommitHook();
        
        status = Status.TRANSACTION_COMMITTED;

        theChangeSet.freeze();
    }

    /**
     * This hook is invoked just prior to committing the Transaction. This allows subclasses to hook
     * in before the commit actually happens.
     */
    protected void preCommitHook()
    {
        // no op on this leve
    }

    /**
     * Determine whether the calling Thread is compatible with this Transaction.
     *
     * @throws IllegalTransactionThreadException if the calling Thread is not
     *         compatible with this Transaction
     */
    public void checkThreadIsAllowed()
        throws
            IllegalTransactionThreadException
    {
        // FIXME? This should perhaps be somewhat looser than this: Threads should be able to
        // "empower" other threads to do work within a transaction on their behalf

        if( myThread != Thread.currentThread() ) {
            throw new IllegalTransactionThreadException( theTransactable );
        }
    }

    /**
      * Obtain the current status of this Transaction.
      *
      * @return the current status of this Transaction
      */
    public Status getStatus()
    {
        return status;
    }

    /**
      * Obtain the set of Changes that have been made as part of this
      * Transaction so far. Depending on the state of the Transaction,
      * the ChangeSet might be complete, frozen or not.
      *
      * @return the set of Changes up to this point
      */
    public ChangeSet getChangeSet()
    {
        return theChangeSet;
    }

    /**
      * Add a Change to the ChangeSet. This shall not be invoked by the
      * application programmer; InfoGrid internals do.
      *
      * We don't synchronize, as it is unlikely that this method is invoked from the wrong Thread.
      *
      * @param newChange the Change to be added to this Transaction
      */
    public void addChange(
            Change newChange )
    {
        theChangeSet.addChange( newChange );
    }

    /**
     * Add a new listener object to this set using a WeakReference.
     *
     * @param newListener the listener to be added to this set
     */
    public void addWeakTransactionListener(
            TransactionListener newListener )
    {
        theListeners.addWeak( newListener );
    }
    
    /**
     * Add a new listener object to this set using a SoftReference.
     *
     * @param newListener the listener to be added to this set
     */
    public void addSoftTransactionListener(
            TransactionListener newListener )
    {
        theListeners.addSoft( newListener );
    }
    
    /**
     * Add a new listener object to this set directly, i.e. without using References.
     *
     * @param newListener the listener to be added to this set
     */
    public void addTransactionListener(
            TransactionListener newListener )
    {
        theListeners.addDirect( newListener );
    }

    /**
     * Internal helper to createCopy a listener set.
     * 
     * @return the created listener set
     */
    protected FlexibleListenerSet<TransactionListener,Transaction,Status> createListenerSet()
    {
        FlexibleListenerSet<TransactionListener,Transaction,Status> ret
                = new FlexibleListenerSet<TransactionListener,Transaction,Status>() {
                        /**
                         * Fire the event to one contained object.
                         *
                         * @param listener the receiver of this event
                         * @param event the sent event
                         * @param parameter dispatch parameter
                         */
                        protected void fireEventToListener(
                                TransactionListener listener,
                                Transaction         event,
                                Status              parameter )
                        {
                            if( status == Status.TRANSACTION_STARTED ) {
                                listener.transactionStarted( Transaction.this );
                            // } else if( status == TRANSACTION_VOTED ) {
                            //     theListener.transactionVoted( this );
                            } else if( status == Status.TRANSACTION_COMMITTED ) {
                                listener.transactionCommitted( Transaction.this );
                            // } else if( status == TRANSACTION_ROLLEDBACK ) {
                            //     theListener.transactionRolledBack( this );
                            } else {
                                log.error( "unknown value for status in Transaction" );
                            }
                        }
                };
        return ret;
    }

    /**
     * Remove a TransactionListener from this Transaction.
     *
     * @param oldListener the new listener to be removed
     * @see #addTransactionListener
     */
    public synchronized void removeTransactionListener(
            TransactionListener oldListener )
    {
        if( theListeners != null ) {
            theListeners.remove( oldListener );
        }
    }

    /**
      * This internal helper notifies all listeners that the state
      * of this Transaction has changed.
      */
    protected void notifyStateChanged()
    {
        FlexibleListenerSet<TransactionListener,Transaction,Status> listeners = theListeners;
        if( listeners != null ) {
            listeners.fireEvent( this, status );
        }
    }

    /**
     * Convert this object to a string, for debugging.
     *
     * @return this instance in string format
     */
    @Override
    public String toString()
    {
        StringWriter theWriter = new StringWriter( 100 );
        myInvoker.printStackTrace( new PrintWriter( theWriter ));

        return StringHelper.objectLogString(
                this,
                new String[] {
                    "transactable",
                    "changes",
                    "myThread",
                    "started"
                },
                new Object[] {
                    theTransactable,
                    theChangeSet,
                    myThread,
                    theWriter.toString()
                });
    }

    /**
     * Obtain the TransactionConstructionMarker that marks how and where this Transaction was created.
     * For debugging only.
     *
     * @return the TransactionConstructionMarker that marks when this Transaction was created
     */
    public TransactionConstructionMarker getStartMarker()
    {
        return myInvoker;
    }

    /**
      * The Thread on which this Transaction was started and on which it
      * needs to be executed.
      */
    protected Thread myThread;

    /**
     * An Exception created in the constructor of this Transaction, so we can
     * determine "whose" Transaction this is.
     */
    protected TransactionConstructionMarker myInvoker;

    /**
      * The current status of this Transaction.
      */
    protected Status status;

    /**
      * The set of Changes accumulated during this Transaction so far.
      */
    protected ChangeSet theChangeSet = new ChangeSet();

    /**
      * The transactable that we belong to.
      */
    protected MeshBase theTransactable;

    /**
      * The set of TransactionListeners. Allocated as needed.
      */
    protected FlexibleListenerSet<TransactionListener,Transaction,Status> theListeners = null;

    /**
     * The number of attempts of recalculations we do before we declare this a
     * circular loop and give up.
     */
    protected static final int MAX_RECALCULATE_ATTEMPTS = 100;

    /**
     * Defines the values for a transaction status. FIXME: Not all of these values
     * have been implemented so far.
     */
    public static enum Status
    {
        /**
          * Indicates that this Transaction is currently being started.
          */
        TRANSACTION_BEING_STARTED,

        /**
          * Indicates that this Transaction has been started but not voted on.
          */
        TRANSACTION_STARTED,

        /**
          * Indicates that this Transaction has voted but not committed or rollback'ed yet.
          */
        TRANSACTION_VOTED,

        /**
          * Indicates that this Transaction has been committed.
          */
        TRANSACTION_COMMITTED,

        /**
          * Indicates that this Transaction has been rolled back.
          */
        TRANSACTION_ROLLEDBACK
    }
}
