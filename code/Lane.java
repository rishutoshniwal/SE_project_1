
/* $Id$
 *
 * Revisions:
 *   $Log: Lane.java,v $
 *   Revision 1.52  2003/02/20 20:27:45  ???
 *   Fouls disables.
 *
 *   Revision 1.51  2003/02/20 20:01:32  ???
 *   Added things.
 *
 *   Revision 1.50  2003/02/20 19:53:52  ???
 *   Added foul support.  Still need to update laneview and test this.
 *
 *   Revision 1.49  2003/02/20 11:18:22  ???
 *   Works beautifully.
 *
 *   Revision 1.48  2003/02/20 04:10:58  ???
 *   Score reporting code should be good.
 *
 *   Revision 1.47  2003/02/17 00:25:28  ???
 *   Added disbale controls for View objects.
 *
 *   Revision 1.46  2003/02/17 00:20:47  ???
 *   fix for event when game ends
 *
 *   Revision 1.43  2003/02/17 00:09:42  ???
 *   fix for event when game ends
 *
 *   Revision 1.42  2003/02/17 00:03:34  ???
 *   Bug fixed
 *
 *   Revision 1.41  2003/02/16 23:59:49  ???
 *   Reporting of sorts.
 *
 *   Revision 1.40  2003/02/16 23:44:33  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.39  2003/02/16 23:43:08  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.38  2003/02/16 23:41:05  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.37  2003/02/16 23:00:26  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.36  2003/02/16 21:31:04  ???
 *   Score logging.
 *
 *   Revision 1.35  2003/02/09 21:38:00  ???
 *   Added lots of comments
 *
 *   Revision 1.34  2003/02/06 00:27:46  ???
 *   Fixed a race condition
 *
 *   Revision 1.33  2003/02/05 11:16:34  ???
 *   Boom-Shacka-Lacka!!!
 *
 *   Revision 1.32  2003/02/05 01:15:19  ???
 *   Real close now.  Honest.
 *
 *   Revision 1.31  2003/02/04 22:02:04  ???
 *   Still not quite working...
 *
 *   Revision 1.30  2003/02/04 13:33:04  ???
 *   Lane may very well work now.
 *
 *   Revision 1.29  2003/02/02 23:57:27  ???
 *   fix on pinsetter hack
 *
 *   Revision 1.28  2003/02/02 23:49:48  ???
 *   Pinsetter generates an event when all pins are reset
 *
 *   Revision 1.27  2003/02/02 23:26:32  ???
 *   ControlDesk now runs its own thread and polls for free lanes to assign queue members to
 *
 *   Revision 1.26  2003/02/02 23:11:42  ???
 *   parties can now play more than 1 game on a lane, and lanes are properly released after games
 *
 *   Revision 1.25  2003/02/02 22:52:19  ???
 *   Lane compiles
 *
 *   Revision 1.24  2003/02/02 22:50:10  ???
 *   Lane compiles
 *
 *   Revision 1.23  2003/02/02 22:47:34  ???
 *   More observering.
 *
 *   Revision 1.22  2003/02/02 22:15:40  ???
 *   Add accessor for pinsetter.
 *
 *   Revision 1.21  2003/02/02 21:59:20  ???
 *   added conditions for the party choosing to play another game
 *
 *   Revision 1.20  2003/02/02 21:51:54  ???
 *   LaneEvent may very well be observer method.
 *
 *   Revision 1.19  2003/02/02 20:28:59  ???
 *   fixed sleep thread bug in lane
 *
 *   Revision 1.18  2003/02/02 18:18:51  ???
 *   more changes. just need to fix scoring.
 *
 *   Revision 1.17  2003/02/02 17:47:02  ???
 *   Things are pretty close to working now...
 *
 *   Revision 1.16  2003/01/30 22:09:32  ???
 *   Worked on scoring.
 *
 *   Revision 1.15  2003/01/30 21:45:08  ???
 *   Fixed speling of received in Lane.
 *
 *   Revision 1.14  2003/01/30 21:29:30  ???
 *   Fixed some MVC stuff
 *
 *   Revision 1.13  2003/01/30 03:45:26  ???
 *   *** empty log message ***
 *
 *   Revision 1.12  2003/01/26 23:16:10  ???
 *   Improved thread handeling in lane/controldesk
 *
 *   Revision 1.11  2003/01/26 22:34:44  ???
 *   Total rewrite of lane and pinsetter for R2's observer model
 *   Added Lane/Pinsetter Observer
 *   Rewrite of scoring algorythm in lane
 *
 *   Revision 1.10  2003/01/26 20:44:05  ???
 *   small changes
 *
 * 
 */

import java.util.Vector;
import java.util.Iterator;
import java.util.Date;

public class Lane extends Thread implements PinsetterObserver {	
	private Party party;
	private Pinsetter setter;
	

	private boolean gameIsHalted;

	private boolean partyAssigned;
	private boolean gameFinished;
	private Iterator bowlerIterator;

	private int ball;
	private int bowlIndex;
	private int frameNumber;
	private boolean tenthFrameStrike;

	private boolean canThrowAgain;
	
	
	private int gameNumber;
	
	private Bowler currentThrower;			// = the thrower who just took a throw

	private LaneSubscriber laneSubscriber;
	private ScoreStatus scoreStatus;
	/** Lane()
	 * 
	 * Constructs a new lane and starts its thread
	 * 
	 * @pre none
	 * @post a new lane has been created and its thered is executing
	 */
	public Lane() { 
		setter = new Pinsetter();
		
		scoreStatus=new ScoreStatus();
		
		laneSubscriber = new LaneSubscriber();

		gameIsHalted = false;
		partyAssigned = false;

		gameNumber = 0;
        
		setter.subscribe( this );
		
		this.start();
	}




	

	private void gameNotFinishedPreCheck(){
		while (gameIsHalted) {
			try {
				sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void throwerFun(){

		canThrowAgain = true;
		tenthFrameStrike = false;
		ball = 0;

		while (canThrowAgain) {
			setter.ballThrown();		// simulate the thrower's ball hiting
			ball++;
		}
		
		if (frameNumber == 9){
			// finalScores[bowlIndex][gameNumber] = cumulScores[bowlIndex][9];
			scoreStatus.setFinalScore(bowlIndex, gameNumber, scoreStatus.getCumulScore(bowlIndex, 9));

			try{
			Date date = new Date();
			String dateString = "" + date.getHours() + ":" + date.getMinutes() + " " + date.getMonth() + "/" + date.getDay() + "/" + (date.getYear() + 1900);
			// ScoreHistoryFile.addScore(currentThrower.getNick(), dateString, new Integer(cumulScores[bowlIndex][9]).toString());
			ScoreHistoryFile.addScore(currentThrower.getNickName(), dateString, new Integer(scoreStatus.getCumulScore(bowlIndex, 9)).toString());
			} catch (Exception e) {System.err.println("Exception in addScore. "+ e );} 
		}

		
		setter.reset();
		bowlIndex++;
	}

	private void gameNotFinishedFun(){

		if (bowlerIterator.hasNext()) {
			currentThrower = (Bowler)bowlerIterator.next();

			throwerFun();
			
		} else {
			frameNumber++;
			resetBowlerIterator();

			bowlIndex = 0;
			if (frameNumber > 9) {
				gameFinished = true;
				gameNumber++;
			}
		}
	}

	private void bowlerFun(Bowler thisBowler, int myIndex, Vector printVector){

		ScoreReport sr = new ScoreReport( thisBowler, scoreStatus.getFinalScore(myIndex), gameNumber );
		sr.sendEmail(thisBowler.getEmail());
		Iterator printIt = printVector.iterator();
		while (printIt.hasNext()){
			if (thisBowler.getNickName().equals( (String)printIt.next())){
				System.out.println("Printing " + thisBowler.getNickName());
				sr.sendPrintout();
			}
		}
	}

	private void dontWantToPlayFun(){
		partyAssigned = false;

		Vector printVector;	
		EndGameReport egr = new EndGameReport( ((Bowler)party.getMembers().get(0)).getNickName() + "'s Party", party);
		printVector = egr.getResult();

		Iterator scoreIt = party.getMembers().iterator();
		party = null;
		
		
		laneSubscriber.publish(lanePublish());
		
		int myIndex = 0;
		while (scoreIt.hasNext()){
			Bowler thisBowler = (Bowler)scoreIt.next();
			// ScoreReport sr = new ScoreReport( thisBowler, finalScores[myIndex++], gameNumber );
			
			bowlerFun(thisBowler, myIndex, printVector);
			myIndex++;
		}
	}

	private void gameFinishedFun(){
		EndGamePrompt egp = new EndGamePrompt( ((Bowler) party.getMembers().get(0)).getNickName() + "'s Party" );
		int result = egp.getResult();
		egp.distroy();
		
		
		System.out.println("result was: " + result);
		
		// TODO: send record of scores to control desk
		if (result == 1) {					// yes, want to play again
			scoreStatus.resetScores(this);
			resetBowlerIterator();
			
		} else if (result == 2) {// no, dont want to play another game
			
			dontWantToPlayFun();
		}
	}
	
	/** run()
	 * 
	 * entry point for execution of this lane 
	 */
	public void run() {
		
		while (true) {
			if (partyAssigned && !gameFinished) {	// we have a party on this lane, 
			// so next bower can take a throw
			
				gameNotFinishedPreCheck();
				gameNotFinishedFun();

			} else if (partyAssigned ) {
				
				gameFinishedFun();
			}
						
			try {
				sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	
	private void lastFrameFun(PinsetterEvent pe){
		if (pe.totalPinsDown() == 10) {
			setter.resetPins();
			if(pe.getThrowNumber() == 1) {
				tenthFrameStrike = true;
			}
		}
	
		if ((pe.totalPinsDown() != 10) && (pe.getThrowNumber() == 2 && tenthFrameStrike == false)) {
			canThrowAgain = false;
			//laneSubscriber.publish( lanePublish() );
		}
	
		if (pe.getThrowNumber() == 3) {
			canThrowAgain = false;
			//laneSubscriber.publish( lanePublish() );
		}
	}

	private void nonLastFrameFun(PinsetterEvent pe){
		if (pe.pinsDownOnThisThrow() == 10) {		// threw a strike
			canThrowAgain = false;
			//laneSubscriber.publish( lanePublish() );
		} else if (pe.getThrowNumber() == 2) {
			canThrowAgain = false;
			//laneSubscriber.publish( lanePublish() );
		} else if (pe.getThrowNumber() == 3)  
			System.out.println("I'm here...");
	}

	private void handleFrameFun(PinsetterEvent pe){
		if (frameNumber == 9) {
			lastFrameFun(pe);
		} else { // its not the 10th frame
	
			nonLastFrameFun(pe);
		}
	}
	
	/** recievePinsetterEvent()
	 * 
	 * recieves the thrown event from the pinsetter
	 *
	 * @pre none
	 * @post the event has been acted upon if desiered
	 * 
	 * @param pe 		The pinsetter event that has been received.
	 */
	public void receivePinsetterEvent(PinsetterEvent pe) {
	
		if (pe.pinsDownOnThisThrow() >=  0) {			// this is a real throw
			scoreStatus.markScore(this, currentThrower, frameNumber + 1, pe.getThrowNumber(), pe.pinsDownOnThisThrow(), bowlIndex);

			// next logic handles the ?: what conditions dont allow them another throw?
			// handle the case of 10th frame first
			handleFrameFun(pe);
		} 
	}
	



	/** resetBowlerIterator()
	 * 
	 * sets the current bower iterator back to the first bowler
	 * 
	 * @pre the party as been assigned
	 * @post the iterator points to the first bowler in the party
	 */
	private void resetBowlerIterator() {
		bowlerIterator = (party.getMembers()).iterator();
	}
		
	/** assignParty()
	 * 
	 * assigns a party to this lane
	 * 
	 * @pre none
	 * @post the party has been assigned to the lane
	 * 
	 * @param theParty		Party to be assigned
	 */
	public void assignParty( Party theParty ) {
		party = theParty;
		resetBowlerIterator();
		partyAssigned = true;
	
		scoreStatus.resetScoreAray(this);
		gameNumber = 0;
		
		scoreStatus.resetScores(this);
	}

	public Party getParty(){
		return party;
	}
	
	
	public void setGameFinished(boolean input){
		gameFinished = input;
	}

	
	public void setFrameNumber(int input){
		frameNumber = input;
	}

	/** lanePublish()
	 *
	 * Method that creates and returns a newly created laneEvent
	 * 
	 * @return		The new lane event
	 */
	public LaneEvent lanePublish(  ) {
		return new LaneEvent(party, bowlIndex, currentThrower, scoreStatus.getCumulScores(), scoreStatus.getScore(), frameNumber+1, scoreStatus.getCurScores(), ball, gameIsHalted);
	}
	
	/** isPartyAssigned()
	 * 
	 * checks if a party is assigned to this lane
	 * 
	 * @return true if party assigned, false otherwise
	 */
	public boolean isPartyAssigned() {
		return partyAssigned;
	}
	
	/** isGameFinished
	 * 
	 * @return true if the game is done, false otherwise
	 */
	public boolean isGameFinished() {
		return gameFinished;
	}

	public LaneSubscriber getLaneSubscriber(){
		return laneSubscriber;
	}

	
	public Pinsetter getPinsetter() {
		return setter;	
	}

	/**
	 * Pause the execution of this game
	 */
	public void pauseGame() {
		gameIsHalted = true;
		laneSubscriber.publish(lanePublish());
	}
	
	/**
	 * Resume the execution of this game
	 */
	public void unPauseGame() {
		gameIsHalted = false;
		laneSubscriber.publish(lanePublish());
	}

}
