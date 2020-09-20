package models;

public class Wagon {
    protected int id;                 // some unique ID of a Wagon
    private Wagon nextWagon;        // another wagon that is appended at the tail of this wagon
                                    // a.k.a. the successor of this wagon in a sequence
                                    // set to null if no successor is connected
    private Wagon previousWagon;    // another wagon that is prepended at the front of this wagon
                                    // a.k.a. the predecessor of this wagon in a sequence
                                    // set to null if no predecessor is connected


    // representation invariant propositions:
    // tail-connection-invariant:   wagon.nextWagon == null or wagon == wagon.nextWagon.previousWagon
    // front-connection-invariant:  wagon.previousWagon == null or wagon = wagon.previousWagon.nextWagon

    public Wagon (int wagonId) {
        this.id = wagonId;
    }

    public Wagon() {

    }

    public int getId() {
        return id;
    }

    public Wagon getNextWagon() {
        return nextWagon;
    }

    public Wagon getPreviousWagon() {
        return previousWagon;
    }

    public void setNextWagon(Wagon nextWagon) {
        this.nextWagon = nextWagon;
    }

    public void setPreviousWagon(Wagon previousWagon) {
        this.previousWagon = previousWagon;
    }

    /**
     * @return  whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {

        return nextWagon != null;
    }

    /**
     * @return  whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {

        return previousWagon != null;
    }

    /**
     * finds the last wagon of the sequence of wagons attached to this wagon
     * if no wagons are attached return this wagon itselves
     * @return  the wagon found
     */
    public Wagon getLastWagonAttached() {
        // TODO provide an iterative solution (without recursion)

        Wagon nextWagon = getNextWagon();

        if(nextWagon == null){
            return this;
        }

        while (nextWagon.getNextWagon() != null){
            nextWagon = nextWagon.getNextWagon();
        }

        return nextWagon;
    }

    /**
     * @return  the number of wagons appended to this wagon
     *          return 0 if no wagons have been appended.
     */
    public int getSequenceLength() {
        // TODO provide a recursive solution

        int trainLength;
        Wagon currentWagon = this;

        if (currentWagon.getNextWagon() == null && currentWagon.getPreviousWagon() == null) return 1;

        trainLength = 1;
        while (currentWagon.hasNextWagon()) {
            currentWagon = currentWagon.getNextWagon();
            trainLength++;
        }

        return trainLength;
    }

    /**
     * attaches this wagon at the tail of a given prevWagon.
     * @param newPreviousWagon
     * @throws RuntimeException if this wagon already has been appended to a wagon.
     * @throws RuntimeException if prevWagon already has got a wagon appended.
     */
    public void attachTo(Wagon newPreviousWagon) {
        // TODO verify the exceptions WIP
            newPreviousWagon.setNextWagon(this);
            this.setPreviousWagon(newPreviousWagon);
//            throw new RuntimeException("This wagon has already been appended to a wagon");

        // TODO attach this wagon to its new predecessor (sustaining the invariant propositions).
//        throw new RuntimeException("Previous wagon has already got a wagon appended");

    }

    /**
     * detaches this wagon from its previous wagons.
     * no action if this wagon has no previous wagon attached.
     */
    public void detachFromPrevious() {
        // TODO detach this wagon from its predecessors (sustaining the invariant propositions).
        if (this.hasPreviousWagon()) {
            getPreviousWagon().setPreviousWagon(null);// to get previous wagon
            setPreviousWagon(null); //remove to prev wagon
        }
    }

    /**
     * detaches this wagon from its tail wagons.
     * no action if this wagon has no succeeding next wagon attached.
     */
    public void detachTail() {
        // TODO detach this wagon from its successors (sustaining the invariant propositions).
        getNextWagon().setPreviousWagon(null);
        setNextWagon(null);

    }

    /**
     * attaches this wagon at the tail of a given newPreviousWagon.
     * if required, first detaches this wagon from its current predecessor
     * and/or detaches the newPreviousWagon from its current successor
     * @param newPreviousWagon
     */
    public void reAttachTo(Wagon newPreviousWagon) {
        // TODO detach any existing connections that will be rearranged
        getPreviousWagon().setNextWagon(null);
        newPreviousWagon.setNextWagon(this);
        // TODO attach this wagon to its new predecessor (sustaining the invariant propositions).
        setPreviousWagon(newPreviousWagon);
    }

    /**
     * Removes this wagon from the sequence that it is part of, if any.
     * Reconnect the subsequence of its predecessors with the subsequence of its successors, if any.
     */
    public void removeFromSequence() {
        // TODO
        if (hasNextWagon() && hasPreviousWagon()) {
            getNextWagon().reAttachTo(getPreviousWagon());
            setNextWagon(null);
            setPreviousWagon(null);
        } else if (hasPreviousWagon() && !hasNextWagon()) {
            getPreviousWagon().setNextWagon(null);
            setPreviousWagon(null);
        } else if (!hasPreviousWagon() && hasNextWagon()) {
            getNextWagon().setPreviousWagon(null);
            setNextWagon(null);
        }
    }


    /**
     * reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the predecessor of this Wagon, if any.
     * no action if this Wagon has no succeeding next wagon attached.
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        // TODO provide a recursive implementation

        return null;
    }


    // TODO
}
