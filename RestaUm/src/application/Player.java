package application;

import java.io.Serializable;

public class Player implements Serializable{
	
	private String name;
	
	private boolean isTurn;
	
	private boolean winner;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean getIsTurn() {
		return isTurn;
	}
	
	public void setIsTurn(boolean isTurn) {
		this.isTurn = isTurn;
	}
	
	public boolean getWinner() {
		return winner;
	}
	
	public void setWinner(boolean winner) {
		this.winner = winner;
	}
	
	public Player(String name, boolean isTurn) {
		this.name = name;
		this.isTurn = isTurn;
		this.winner = false;
	}

}
