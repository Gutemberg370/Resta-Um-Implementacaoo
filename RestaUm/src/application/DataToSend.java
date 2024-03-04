package application;

import java.io.Serializable;

public class DataToSend implements Serializable{
	
	public TypeOfDataSent typeOfDataSent;
	
	public int oldX;
	
	public int oldY;
	
	public int newX;
	
	public int newY;
	
	public int removedX;
	
	public int removedY;
	
	public Tile[][] mainBoard;
	
	public String message;
	
	public Player opponent;
	
	public GameResult gameResult;
	
	public DataToSend(TypeOfDataSent typeOfDataSent, Player opponent) {
		this.typeOfDataSent = typeOfDataSent;
		this.opponent = opponent;
		
	}
	
	public DataToSend(TypeOfDataSent typeOfDataSent, int oldX, int oldY, int newX,  int newY, int removedX, int removedY, Tile[][] mainBoard) {
		this.typeOfDataSent = typeOfDataSent;
		this.oldX = oldX;
		this.oldY = oldY;
		this.newX = newX;
		this.newY = newY;
		this.removedX = removedX;
		this.removedY = removedY;
		this.mainBoard = mainBoard;
		
	}
	
	public DataToSend(TypeOfDataSent typeOfDataSent, String message) {
		this.typeOfDataSent = typeOfDataSent;
		this.message = message;
		
	}
	
	public DataToSend(TypeOfDataSent typeOfDataSent, Player opponent, GameResult gameResult) {
		this.typeOfDataSent = typeOfDataSent;
		this.opponent = opponent;
		this.gameResult = gameResult;
		
	}
	
	public DataToSend(TypeOfDataSent typeOfDataSent) {
		this.typeOfDataSent = typeOfDataSent;
		
	}

}
