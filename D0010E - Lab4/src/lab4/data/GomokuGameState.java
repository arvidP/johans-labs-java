/*
 * Created on 2007 feb 8
 */
package lab4.data;

import java.util.Observable;
import java.util.Observer;

import lab4.client.GomokuClient;
import lab4.data.GameGrid.squareState;

/** Represents the state of a game
 * @author Johan Str�m
 * @author Afshin Aresh
 * 
 */

public class GomokuGameState extends Observable implements Observer{

   // Game variables
	private final int DEFAULT_SIZE = 15;
	private GameGrid gameGrid;
	
	
    //Possible game states
    enum gameState {
		NOT_STARTED, MY_TURN, OTHER_TURN, FINISHED
	};
	
	gameState currentState;
	
	private GomokuClient client;
	
	private String message;
	
	/**
	 * The constructor
	 * 
	 * @param gc The client used to communicate with the other player
	 */
	public GomokuGameState(GomokuClient gc){
		client = gc;
		client.addObserver(this);
		gc.setGameState(this);
		gameGrid = new GameGrid(DEFAULT_SIZE);
	}
	

	/**
	 * Returns the message string
	 * 
	 * @return the message string
	 */
	public String getMessageString(){
		return message;
	}
	
	/**
	 * Returns the game grid
	 * 
	 * @return the game grid
	 */
	public GameGrid getGameGrid(){
		return gameGrid;
	}

	/**
	 * This player makes a move at a specified location
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void move(int x, int y){

		if(currentState == gameState.MY_TURN){
			if(gameGrid.getLocation(x, y) == squareState.EMPTY){
				gameGrid.move(x, y, squareState.ME);
				client.sendMoveMessage(x, y);
				if(gameGrid.isWinner(squareState.ME)){
					currentState = gameState.FINISHED;
					message = "You Winn!";
				}
				else {
					currentState = gameState.OTHER_TURN;
					message = "Other turn.";
				}
			}
			setChanged();
			notifyObservers();
		}
	}
	
	/**
	 * Starts a new game with the current client
	 */
	public void newGame() {
		if (currentState != gameState.NOT_STARTED) {
			client.sendNewGameMessage();
			gameGrid.clearGrid();
			currentState = gameState.OTHER_TURN;
			message = "Other turn!";
			setChanged();
			notifyObservers();
		}	
	}
	
	/**
	 * Other player has requested a new game, so the 
	 * game state is changed accordingly
	 */
	public void receivedNewGame(){
		gameGrid.clearGrid();
		currentState = gameState.MY_TURN;
		message = "Your turn!";
		setChanged();
		notifyObservers();
		
	}
	
	/**
	 * The connection to the other player is lost, 
	 * so the game is interrupted
	 */
	public void otherGuyLeft(){
		gameGrid.clearGrid();
		currentState = gameState.FINISHED;
		message = "Other guy left. Game finnished!";
		setChanged();
		notifyObservers();
	}
	
	/**
	 * The player disconnects from the client
	 */
	public void disconnect(){
		client.disconnect();
		gameGrid.clearGrid();
		currentState = gameState.FINISHED;
		message = "You disconnected. Game finnished!";
		setChanged();
		notifyObservers();
	}
	
	/**
	 * The player receives a move from the other player
	 * 
	 * @param x The x coordinate of the move
	 * @param y The y coordinate of the move
	 */
	public void receivedMove(int x, int y){
		if(gameGrid.move(x, y, squareState.OTHER)){
			if(gameGrid.isWinner(squareState.OTHER)){
				currentState = gameState.FINISHED;
				message = "You lose the game!";
			}
			else{
				currentState = gameState.MY_TURN;
				message = "It's your turn!";
			}
			setChanged();
			notifyObservers();
		}
	}
	
	public void update(Observable o, Object arg) {
		
		switch(client.getConnectionStatus()){
		case GomokuClient.CLIENT:
			message = "Game started, it is your turn!";
			currentState = gameState.MY_TURN;
			break;
		case GomokuClient.SERVER:
			message = "Game started, waiting for other player...";
			currentState = gameState.OTHER_TURN;
			break;
		}
		setChanged();
		notifyObservers();
		
		
	}
	
}
