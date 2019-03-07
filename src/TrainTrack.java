import java.util.concurrent.atomic.AtomicInteger;

public class TrainTrack {
    private final String[] slots = {"[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]",
            "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]", "[..]"};

    // declare array to hold the Binary Semaphores for access to track slots (sections)
    private final MageeSemaphore slotSem[] = new MageeSemaphore[22];

    // reference to train activity record
    Activity theTrainActivity;

    // global count of trains on shared track
//    AtomicInteger aUsingJunction4;
//    AtomicInteger bUsingJunction4;
//    AtomicInteger aUsingJunction7;
//    AtomicInteger bUsingJunction7;

    // counting semaphore to limit number of trains on track
    MageeSemaphore aCountSem;
    MageeSemaphore bCountSem;

    // declare  Semaphores for mutually exclusive access to aUsingJunction4
    private final MageeSemaphore MutexSemJ4;
    // declare  Semaphores for mutually exclusive access to bUsingJunction4
    private final MageeSemaphore MutexSemJ7;

    // shared track lock
    MageeSemaphore Junction4;
    MageeSemaphore Junction7;

    /* Constructor for TrainTrack */
    public TrainTrack() {
        // record the train activity
        theTrainActivity = new Activity(slots);
        // create the array of slotSems and set them all free (empty)
        for (int i = 0; i < 22; i++) {
            slotSem[i] = new MageeSemaphore(1);
        }
        // create  semaphores for mutually exclusive access to global count
        MutexSemJ4 = new MageeSemaphore(1);
        MutexSemJ7 = new MageeSemaphore(1);
        // create global AtomicInteger count variables
//        aUsingJunction4 = new AtomicInteger(0);
//        bUsingJunction4 = new AtomicInteger(0);
//        aUsingJunction7 = new AtomicInteger(0);
//        bUsingJunction7 = new AtomicInteger(0);
        // create  semaphores for limiting number of trains on track
        aCountSem = new MageeSemaphore(4);
        bCountSem = new MageeSemaphore(4);
    }  // constructor

    public void trainA_MoveOnToTrack(String trainName) {
        CDS.idleQuietly((int) (Math.random() * 100));
        aCountSem.P(); // limit  number of trains on track to avoid deadlock
        // record the train activity
        slotSem[0].P();// wait for slot 0 to be free
        slots[0] = "[" + trainName + "]"; // move train type A on to slot zero
        theTrainActivity.addMovedTo(0); // record the train activity
    }// end trainA_movedOnToTrack

    public void trainB_MoveOnToTrack(String trainName) {
        // record the train activity
        bCountSem.P();  // limit  number of trains on track to avoid deadlock
        CDS.idleQuietly((int) (Math.random() * 100));
        slotSem[12].P();// wait for slot 12 to be free
        slots[12] = "[" + trainName + "]"; // move train type B on to slot 12
        theTrainActivity.addMovedTo(12); // record the train activity
    }// end trainB_movedOnToTrack

    public void trainA_MoveAroundToSharedTrack(String trainName) {
        CDS.idleQuietly((int) (Math.random() * 100));
        int currentPosition = 0;
        do {
            if(currentPosition == 3){
                MutexSemJ4.P();
                slotSem[5].P();
                slots[5] = slots[3];
                slots[3] = "[..]";
                MutexSemJ4.V();
                slotSem[3].V();
                theTrainActivity.addMovedTo(5);
                currentPosition = 5;

            } else if(currentPosition == 6) {
                MutexSemJ7.P();
                slotSem[8].P();
                slots[8] = slots[6];
                slots[6] = "[..]";
                MutexSemJ7.V();
                slotSem[6].V();
                theTrainActivity.addMovedTo(8);
                currentPosition = 8;
            } else {
                // wait until the position ahead is empty and then move into it
                slotSem[currentPosition + 1].P(); // wait for the slot ahead to be free
                slots[currentPosition + 1] = slots[currentPosition]; // move train forward one position
                slots[currentPosition] = "[..]"; // clear the slot the train vacated
                theTrainActivity.addMovedTo(currentPosition + 1); // record the train activity
                slotSem[currentPosition].V(); // signal slot you are leaving
                currentPosition++;
            }
        } while (currentPosition < 11);
        CDS.idleQuietly((int) (Math.random() * 100));
    } // end trainA_MoveAroundToSharedTrack

    public void trainB_MoveAroundToSharedTrack(String trainName) {
        CDS.idleQuietly((int) (Math.random() * 100));
        int currentPosition = 12;
        do {
            if(currentPosition == 15){
                MutexSemJ7.P();
                slotSem[16].P();
                slots[16] = slots[15];
                slots[15] = "[..]";
                MutexSemJ7.V();
                slotSem[15].V();
                theTrainActivity.addMovedTo(16);
                currentPosition++;
            } else if(currentPosition == 17){
                MutexSemJ4.P();
                slotSem[18].P();
                slots[18] = slots[17];
                slots[17] = "[..]";
                MutexSemJ4.V();
                slotSem[17].V();
                theTrainActivity.addMovedTo(18);
                currentPosition++;
            } else{
                /* wait until the position ahead is empty and then move into it*/
                slotSem[currentPosition + 1].P(); // wait for the slot ahead to be free
                slots[currentPosition + 1] = slots[currentPosition]; // move train forward
                slots[currentPosition] = "[..]"; //clear the slot the train vacated
                theTrainActivity.addMovedTo(currentPosition - 1); //record the train activity
                slotSem[currentPosition].V(); //signal slot you are leaving
                currentPosition++;
            }

        } while (currentPosition < 21 );
        CDS.idleQuietly((int) (Math.random() * 100));
    } // end trainB_MoveAroundToSharedTrack

//    public void trainAMoveAcrossJunction(String trainName) {
//        // wait for the necessary conditions to get access to shared track
//        MutexSemJ4.P(); // obtain mutually exclusive access to global variable Junction4]
//
//        MutexSemJ4.V(); // release mutually exclusive access to global variable aUsingSharedTrack
//        // move on to shared track
//        slotSem[7].P();
//        slots[7] = slots[6];
//        slots[6] = "[..]";
//        slotSem[6].V(); //move from slot[6] to slot[7]
//        theTrainActivity.addMovedTo(7);  //record the train activity
//        CDS.idleQuietly((int) (Math.random() * 10));
//        // move off shared track
//        slotSem[0].P();
//        slots[0] = slots[9];
//        slots[9] = "[..]";
//        slotSem[9].V(); //move from slot[9] to slot[0]
//        theTrainActivity.addMovedTo(0); // record the train activity
//        // signal conditions when leaving shared track
//        aMutexSem.P(); // obtain mutually exclusive access to global variable aUsingSharedTracK
//        if (aUsingSharedTrack.decrementAndGet() == 0) // if last A train leaving shared track
//        {
//            sharedTrackLock.V(); // release lock to shared track
//        }
//        aMutexSem.V(); // release mutually exclusive access to global variable aUsingSharedTrack
//        CDS.idleQuietly((int) (Math.random() * 10));
//    }// end   trainAMoveAcrossJunction
//
//    public void trainBMoveAcrossJunction(String trainName) {
//        CDS.idleQuietly((int) (Math.random() * 10));
//        // wait for the necessary conditions to get access to shared track
//        bMutexSem.P(); // obtain mutually exclusive access to global variable bUsingSharedTrack
//        if (bUsingSharedTrack.incrementAndGet() == 1)// if first B train joining shared track
//        {
//            sharedTrackLock.P();  // grab lock to shared track
//        }
//        bMutexSem.V(); // release mutually exclusive access to global variable bUsingSharedTrack
//        CDS.idleQuietly((int) (Math.random() * 10));
//        // move on to shared track
//        slotSem[9].P();
//        slots[9] = slots[10];
//        slots[10] = "[..]";
//        slotSem[10].V(); //move from slot[10] to slot[9]
//        theTrainActivity.addMovedTo(9);  //record the train activity
//        CDS.idleQuietly((int) (Math.random() * 10));
//        // move along shared track
//        slotSem[8].P();
//        slots[8] = slots[9];
//        slots[9] = "[..]";
//        slotSem[9].V(); //move from slot[9] to slot[8]
//        theTrainActivity.addMovedTo(8); // record the train activity
//        slotSem[7].P();
//        slots[7] = slots[8];
//        slots[8] = "[..]";
//        slotSem[8].V(); //move from slot[8] to slot[7]
//        theTrainActivity.addMovedTo(7); // record the train activity
//        CDS.idleQuietly((int) (Math.random() * 10));
//        // move off shared track
//        slotSem[16].P();
//        slots[16] = slots[7];
//        slots[7] = "[..]";
//        slotSem[7].V(); //move from slot[7] to slot[16]
//        theTrainActivity.addMovedTo(16); // record the train activity
//        // signal conditions when leaving shared track
//        bMutexSem.P(); // obtain mutually exclusive access to global variable aUsingSharedTracK
//        if (bUsingSharedTrack.decrementAndGet() == 0) // if last B train leaving shared track
//        {
//            sharedTrackLock.V(); // release lock to shared track
//        }
//        bMutexSem.V(); // release mutually exclusive access to global variable aUsingSharedTrack
//        CDS.idleQuietly((int) (Math.random() * 10));
//    }// end   trainBMoveAcrossJunction

    public void trainA_MoveOffTrack(String trainName) {
        CDS.idleQuietly((int) (Math.random() * 10));
        // record the train activity
        theTrainActivity.addMessage("Train " + trainName + " is leaving the A loop at section 0");
        slots[11] = "[..]"; // move train type A off slot 11
        slotSem[11].V();// signal slot 11 to be free
        CDS.idleQuietly((int) (Math.random() * 10));
        aCountSem.V(); // signal space for another A train
    }// end trainA_movedOffTrack

    public void trainB_MoveOffTrack(String trainName) {
        CDS.idleQuietly((int) (Math.random() * 10));
        // record the train activity
        theTrainActivity.addMessage("Train " + trainName + " is leaving the B loop at section 16");
        slots[21] = "[..]"; // move train type A off slot 21
        slotSem[21].V();// signal slot 21 to be free
        CDS.idleQuietly((int) (Math.random() * 10));
        bCountSem.V(); // signal space for another B train
    }// end trainB_movedOffTrack




}
