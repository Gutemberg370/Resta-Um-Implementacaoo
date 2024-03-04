package application;

import javafx.scene.paint.Color;
import java.lang.Math;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.VBox;


public class Main extends Application {
	
	public static final int TILE_SIZE = 64;
	public static final int WIDTH = 17;
    public static final int HEIGHT = 11;

    // Server and Client variables 
    private boolean isServer = false;
    private NetworkConnection connection = isServer? createServer(55555) : createClient("127.0.0.1",55555);
    
    
    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();
    private Tile[][] mainBoard = new Tile[WIDTH][HEIGHT];
    private Player player = createPlayer();
    private Label player1 = new Label("Jogador 1");
    private Label player2 = new Label("Jogador 2");
    private TextArea chat = new TextArea();
    private Text endGameText = new Text();
    private ImageView winImage1 = new ImageView(getClass().getResource("win.png").toExternalForm());
    private ImageView winImage2 = new ImageView(getClass().getResource("win.png").toExternalForm());
    private Button withdrawalButton = new Button("Desistir");
    private Button resetButton = new Button("Resetar");
    
    
    // Create login page
    private Parent createLogin() {
    	Pane root = new Pane();
    	
    	BackgroundFill backgroundFill = new BackgroundFill(Color.valueOf("#6194F5"), new CornerRadii(10), new Insets(10));

    	Background background = new Background(backgroundFill);
    	
    	root.setBackground(background);
    	
    	root.setPrefSize(WIDTH/2 * TILE_SIZE, HEIGHT/2 * TILE_SIZE);
    	
    	Label gameName = new Label("RESTA UM");
    	gameName.setFont(new Font("Monaco",36));
    	gameName.setLayoutX(170);
    	gameName.setLayoutY(25);
    	
    	Label title = new Label("Insira o nome do seu jogador e \n clique no botão abaixo para iniciar o jogo");
    	title.setFont(new Font("Arial",18));
    	title.setLayoutX(100);
    	title.setLayoutY(120);
    	title.setMaxWidth(400);
    	title.setTextAlignment(TextAlignment.CENTER);
    	
    	Label name = new Label("Nome :");
    	name.setFont(new Font("Arial",13));
    	name.setLayoutX(95);
    	name.setLayoutY(205);
    	
    	TextField nameInput = new TextField();
    	nameInput.setLayoutX(145);
    	nameInput.setLayoutY(200);
    	nameInput.setMinWidth(220);
    	
    	Button loginButton = new Button("Iniciar Jogo");
    	loginButton.setLayoutX(180);
    	loginButton.setLayoutY(270);
    	loginButton.setMinWidth(150);
    	loginButton.setOnAction(event -> {
    		player.setName(nameInput.getText());
    		if(isServer) {
    			player1.setText(player.getName());
    		}
    		else {
    			player2.setText(player.getName());
    		}
        	Stage window = (Stage)loginButton.getScene().getWindow();
        	Scene scene = new Scene(createContent());
        	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        	window.setScene(scene);
    		DataToSend connectData = new DataToSend(TypeOfDataSent.STARTCONNECTION, player);
        	try {
        		connection.send(connectData);
        	}
        	catch (Exception e1) {
        		chat.appendText("A conexão não pode ser estabelecida.\n");
        	}
        });
    	
    	root.getChildren().addAll(gameName, title, loginButton, name, nameInput);
    	
    	return root;
    }
    
    // Criar tela do jogo
    private Parent createContent() {
    	Pane root = new Pane();
    	
    	// criando a interface do tabuleiro
        endGameText.setFont(new Font("Roboto",30));        
        endGameText.setWrappingWidth(1482);
        endGameText.setY(70);      
        endGameText.setTextAlignment(TextAlignment.CENTER);
              
    	root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
    	root.getChildren().addAll(tileGroup,pieceGroup, endGameText);
        createBoard(tileGroup, pieceGroup,  mainBoard);
        
        // Regra: o servidor é sempre o primeiro a jogar
        if(!isServer) {
        	setPiecesMovement(false);
        }
        
        
        // criando a interface dos jogadores
        ImageView userImage1 = new ImageView(getClass().getResource("user.png").toExternalForm());
        ImageView userImage2 = new ImageView(getClass().getResource("user.png").toExternalForm());
        player1.setFont(new Font("Arial",30));
        player1.setLayoutX(80);
        player1.setLayoutY(64);
        player1.setGraphic(userImage1);
        player1.setGraphicTextGap(20);
        
        player2.setFont(new Font("Arial",30));
        player2.setLayoutX(80);
        player2.setLayoutY(150);
        player2.setGraphicTextGap(20);
        player2.setGraphic(userImage2);
        
        winImage1.setLayoutX(10);
        winImage1.setLayoutY(50);
        
        winImage2.setLayoutX(10);
        winImage2.setLayoutY(135);;
        
        winImage1.setVisible(false);
        winImage2.setVisible(false);
        
        root.getChildren().addAll(player1,player2, winImage1, winImage2);
        
        //criando o botão de resetar o jogo
        resetButton.setLayoutX(50);
        resetButton.setLayoutY(TILE_SIZE * 5);
        resetButton.setMinWidth(TILE_SIZE * 2);
        resetButton.setOnAction(event -> {
        	tileGroup.getChildren().clear();
        	pieceGroup.getChildren().clear();
        	createBoard(tileGroup, pieceGroup,  mainBoard);
        	setPiecesMovement(player.getIsTurn());
	        player.setWinner(false);
	        winImage1.setVisible(false);
	        winImage2.setVisible(false);
	        withdrawalButton.setDisable(false);
	        endGameText.setText("");
	        
    		DataToSend resetData = new DataToSend(TypeOfDataSent.RESETCOMMAND);
        	try {
        		connection.send(resetData);
        	}
        	catch (Exception e1) {
        		chat.appendText("Não foi possível resetar o tabuleiro.\n");
        	}
        });
        
        //fazendo o botão de desistir do jogo
        withdrawalButton.setLayoutX(210);
        withdrawalButton.setLayoutY(TILE_SIZE * 5);
        withdrawalButton.setMinWidth(TILE_SIZE * 2);
        withdrawalButton.setOnAction(event -> {
        	onGameEnded(GameResult.PLAYERGAVEUP);
        	player.setWinner(false);
        	withdrawalButton.setDisable(true);
        });
        
        
        // criando a área do chat		
        chat.setPrefHeight(TILE_SIZE * 5);
        chat.setPrefWidth(TILE_SIZE * 6);
        chat.setEditable(false);
        TextField input = new TextField();
        input.setOnAction(event -> {
        	String message = isServer ? player1.getText() + ": " : player2.getText() + ": ";
        	message += input.getText();
        	input.clear();
        	
        	chat.appendText(message + "\n");
        	DataToSend chatData = new DataToSend(TypeOfDataSent.CHATDATA, message);
        	try {
        		connection.send(chatData);
        	}
        	catch (Exception e) {
        		chat.appendText("A mensagem não conseguiu ser enviada.\n");
        	}
        	
        });
     		
        VBox chatBox = new VBox(0, chat, input);
        chatBox.setLayoutY(360);
     
        root.getChildren().addAll(chatBox,withdrawalButton,resetButton);
        
    	return root;
    }
    
    // Criar o jogador da instância do programa
    private Player createPlayer() {
    	
    	if(isServer) {
    		return new Player("Jogador 1", true);
    	}
    	
    	return new Player("Jogador 2", false);
    	
    }
    
    // Criar a conexão de recebimento de mensagens do servidor
	private Server createServer(int port) {
    	return new Server(port, data->{
    		Platform.runLater(() -> {
    			DataToSend dataReceived = (DataToSend) data;
    			
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.STARTCONNECTION) {
    				player2.setText(dataReceived.opponent.getName());
    			}
    			
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.MOVEDATA) {
    				mainBoard[dataReceived.oldX][dataReceived.oldY].getPiece().move(dataReceived.newX * TILE_SIZE, dataReceived.newY * TILE_SIZE);
    				mainBoard[dataReceived.newX][dataReceived.newY].setPiece(mainBoard[dataReceived.oldX][dataReceived.oldY].getPiece());
    				mainBoard[dataReceived.oldX][dataReceived.oldY].setPiece(null);
    				pieceGroup.getChildren().remove(mainBoard[dataReceived.removedX][dataReceived.removedY].getPiece());
    				mainBoard[dataReceived.removedX][dataReceived.removedY].setPiece(null);
    				player.setIsTurn(true);
    				setPiecesMovement(player.getIsTurn()); 				
    			}
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.CHATDATA) {
    				chat.appendText(dataReceived.message + "\n");
    			}
    			
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.RESETCOMMAND) {
    				tileGroup.getChildren().clear();
    	        	pieceGroup.getChildren().clear();
    	        	createBoard(tileGroup, pieceGroup,  mainBoard);
    	        	setPiecesMovement(player.getIsTurn());
    	        	player.setWinner(false);
    	        	winImage1.setVisible(false);
    	        	winImage2.setVisible(false);
    		        withdrawalButton.setDisable(false);
    		        endGameText.setText("");
    			}
    			
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.RESULTDATA) {
    				
    				if(dataReceived.gameResult == GameResult.NOPIECESLEFT) {
    					setPiecesMovement(false);
    		    		endGameText.setText("Fim do jogo! \n Não há mais peças disponíveis para mover.");
    		    		player.setWinner(false);
    		    		winImage2.setVisible(true);
    		    		withdrawalButton.setDisable(true);
    				}
    				
    				if(dataReceived.gameResult == GameResult.NOMOVESLEFT) {
    					setPiecesMovement(false);
    		    		endGameText.setText("Fim do jogo! \n Não há mais movimentos disponíveis.");
        	    		winImage1.setVisible(true);
        	    		player.setWinner(true);
        	    		withdrawalButton.setDisable(true);
    				}
    				
    				if(dataReceived.gameResult == GameResult.PLAYERGAVEUP) {
    					setPiecesMovement(false);
    					String endText = String.format("Fim do jogo! \n Jogador %s desistiu!", dataReceived.opponent.getName());
    					endGameText.setText(endText);
    					winImage1.setVisible(true);
    					player.setWinner(true);
    					withdrawalButton.setDisable(true);
    				}
    			}
    		});
    	});
    }
    
	// Criar a conexão de recebimento de mensagens do cliente
    private NetworkConnection createClient(String ip, int port) {
    	return new Client(ip, port, data->{
    		Platform.runLater(() -> {
    			DataToSend dataReceived = (DataToSend) data;
    			
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.STARTCONNECTION) {
    				player1.setText(dataReceived.opponent.getName());
    			}
    			
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.MOVEDATA) {
    				mainBoard[dataReceived.oldX][dataReceived.oldY].getPiece().move(dataReceived.newX * TILE_SIZE, dataReceived.newY * TILE_SIZE);
    				mainBoard[dataReceived.newX][dataReceived.newY].setPiece(mainBoard[dataReceived.oldX][dataReceived.oldY].getPiece());
    				mainBoard[dataReceived.oldX][dataReceived.oldY].setPiece(null);
    				pieceGroup.getChildren().remove(mainBoard[dataReceived.removedX][dataReceived.removedY].getPiece());
    				mainBoard[dataReceived.removedX][dataReceived.removedY].setPiece(null);
    				player.setIsTurn(true);
    				setPiecesMovement(player.getIsTurn());				
    			}
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.CHATDATA) {
    				chat.appendText(dataReceived.message + "\n");
    			}
    			
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.RESETCOMMAND) {
    				tileGroup.getChildren().clear();
    	        	pieceGroup.getChildren().clear();
    	        	createBoard(tileGroup, pieceGroup,  mainBoard);
    	        	setPiecesMovement(player.getIsTurn());
    	        	player.setWinner(false);
    	        	winImage1.setVisible(false);
    	        	winImage2.setVisible(false);
    		        withdrawalButton.setDisable(false);
    		        endGameText.setText("");
    			}
    			
    			if(dataReceived.typeOfDataSent == TypeOfDataSent.RESULTDATA) {
    				
    				if(dataReceived.gameResult == GameResult.NOPIECESLEFT) {
    					setPiecesMovement(false);
    		    		endGameText.setText("Fim do jogo! \n Não há mais peças disponíveis para mover.");
    		    		player.setWinner(false);
    		    		winImage1.setVisible(true);
    		    		withdrawalButton.setDisable(true);
    				}
    				
    				if(dataReceived.gameResult == GameResult.NOMOVESLEFT) {
    					setPiecesMovement(false);
    		    		endGameText.setText("Fim do jogo! \n Não há mais movimentos disponíveis.");
    		    		player.setWinner(true);
        	    		winImage2.setVisible(true);
        	    		withdrawalButton.setDisable(true);
    				}
    				
    				if(dataReceived.gameResult == GameResult.PLAYERGAVEUP) {
        				setPiecesMovement(false);
        	    		String endText = String.format("Fim do jogo! \n Jogador %s desistiu!", dataReceived.opponent.getName());
        	    		endGameText.setText(endText);
        	    		player.setWinner(true);
        	    		winImage2.setVisible(true);
        	    		withdrawalButton.setDisable(true);
    				}

    			}
    			
    		});
    	});
	}

	
    // Criar a interface do tabuleiro e o background da tela de jogo inteira
    public void createBoard(Group tileGroup, Group pieceGroup,  Tile[][] board) {
		
    	/////// INTERFACE DE USUÁRIO ///////////
    	
    	// creating tiles for user part
		for(int y = 0; y < HEIGHT; y++) {
			for(int x = 0; x < 6; x++ ) {
				UserTile tile = new UserTile(x , y);
				tileGroup.getChildren().add(tile);
			}
		}
    	
    	
    	/////// INTERFACE DO TABULEIRO ///////////
    	
    	
		//Primeiras duas linhas
		for(int y = 0; y < 2; y++) {
			for(int x = 6; x < Main.WIDTH; x++ ) {
				Tile tile = new Tile(false, x , y);
				board[x][y] = tile;
				tileGroup.getChildren().add(tile);
			}
		}
		
		//Terceira e quarta linhas
		for(int y = 2; y < 4; y++) {
			for(int x = 6; x < Main.WIDTH; x++ ) {
				Tile tile = new Tile( x > 9 && x < 13 ? true : false, x , y);
				board[x][y] = tile;
				tileGroup.getChildren().add(tile);
				
				if(x > 9 && x < 13) {
					Piece piece = makePiece(x , y);
					board[x][y].setPiece(piece);
					pieceGroup.getChildren().add(piece);
				}
			}
		}
		
		//Da quinta até a sétima linhas
		for(int y = 4; y < 7; y++) {
			for(int x = 6; x < Main.WIDTH; x++ ) {
				Tile tile = new Tile( x > 7 && x < 15 ? true : false, x , y);
				board[x][y] = tile;
				tileGroup.getChildren().add(tile);
				
				if(x > 7 && x < 15 && !(x == 11 && y == 5)) {
					Piece piece = makePiece(x , y);
					board[x][y].setPiece(piece);
					pieceGroup.getChildren().add(piece);
				}
				
			}
		
		}
		
		//Oitava e nona linhas
		for(int y = 7; y < 9; y++) {
			for(int x = 6; x < Main.WIDTH; x++ ) {
				Tile tile = new Tile( x > 9 && x < 13 ? true : false, x , y);
				board[x][y] = tile;
				tileGroup.getChildren().add(tile);
				
				if(x > 9 && x < 13) {
					Piece piece = makePiece(x , y);
					board[x][y].setPiece(piece);
					pieceGroup.getChildren().add(piece);
				}
			}
		}
		
		//Duas últimas linhas
		for(int y = 9; y < 11; y++) {
			for(int x = 6; x < Main.WIDTH; x++ ) {
				Tile tile = new Tile(false, x , y);
				board[x][y] = tile;
				tileGroup.getChildren().add(tile);
			}
		}
		
	}
    
    // Verificar se é possível mover a peça para (newX,newY)
    private MoveResult tryMove(Piece piece, int newX, int newY) {
    	
    	//Se eu tentar mover a peça para fora do tabuleiro ou para um lugar do tabuleiro com peça
    	if( newX < 6 || !(mainBoard[newX][newY].getIsPartOfBoard()) || mainBoard[newX][newY].hasPiece()) {
    		return new MoveResult(MoveType.NONE);
    	}
    	
    	int x0 = toBoard(piece.getOldX());
    	int y0 = toBoard(piece.getOldY());
    	
    	//Se eu tentar mover para muito longe
    	if((Math.abs(newX - x0)  + Math.abs(newY - y0)) != 2) {
    		return new MoveResult(MoveType.NONE);
    	}

    	
    	// Se eu tentar mover para a direita
    	if(newX - x0 == 2 && mainBoard[newX-1][newY].hasPiece()) {
    		return new MoveResult(MoveType.RIGHT, mainBoard[newX-1][newY].getPiece());
    	}
    	
    	
    	// Se eu tentar mover para a esquerda
    	if(newX - x0 == -2 && mainBoard[newX+1][newY].hasPiece()) {
    		return new MoveResult(MoveType.LEFT, mainBoard[newX+1][newY].getPiece());
    	}
    	
    	// Se eu tentar mover para baixo
    	if(newY - y0 == 2 && mainBoard[newX][newY-1].hasPiece()) {
    		return new MoveResult(MoveType.DOWN, mainBoard[newX][newY-1].getPiece());
    	}
    	
    	// Se eu tentar mover para cima
    	if(newY - y0 == -2 && mainBoard[newX][newY+1].hasPiece()) {
    		return new MoveResult(MoveType.UP, mainBoard[newX][newY+1].getPiece());
    	}
    	
    	return new MoveResult(MoveType.NONE);
    }
    
    // Converter o valor de pixel para posição na matriz do tabuleiro
    private int toBoard(double pixel) {
    	return (int)(pixel + TILE_SIZE / 2) / TILE_SIZE;
    }
    
    // Criar/Mover peça de acordo com o input do usuário
    private Piece makePiece(int x, int y) {
    	Piece piece = new Piece(x ,y);
    	
    	piece.setOnMouseReleased(e -> {
    		int newX = toBoard(piece.getLayoutX());
    		int newY = toBoard(piece.getLayoutY());
    		
    		MoveResult result = tryMove(piece, newX, newY);
    		
    		int x0 = toBoard(piece.getOldX());
    		int y0 = toBoard(piece.getOldY());
    		
    		switch (result.getType()) {
    			case NONE:
    				piece.abortMove();
    				break;
    			case LEFT:
    				piece.move(newX * TILE_SIZE, newY * TILE_SIZE);
    				mainBoard[newX][newY].setPiece(piece);
    				mainBoard[x0][y0].setPiece(null);
    				mainBoard[newX+1][newY].setPiece(null);
    				pieceGroup.getChildren().remove(result.getPiece());
    				player.setIsTurn(false);
    				setPiecesMovement(player.getIsTurn());
    				onGameEnded(checkIfGameEnded());
    				DataToSend leftMoveData = new DataToSend(TypeOfDataSent.MOVEDATA,x0,y0,newX,newY,newX+1,newY, mainBoard);
    	        	try {
    	        		connection.send(leftMoveData);
    	        	}
    	        	catch (Exception e1) {
    	        		chat.appendText("O movimento para a esquerda não foi registrado.\n");
    	        	}
    				break;
    			case RIGHT:
    				piece.move(newX * TILE_SIZE, newY * TILE_SIZE);
    				mainBoard[newX][newY].setPiece(piece);
    				mainBoard[x0][y0].setPiece(null);
    				mainBoard[newX-1][newY].setPiece(null);
    				pieceGroup.getChildren().remove(result.getPiece());
    				player.setIsTurn(false);
    				setPiecesMovement(player.getIsTurn());
    				onGameEnded(checkIfGameEnded());
    				DataToSend rightMoveData = new DataToSend(TypeOfDataSent.MOVEDATA,x0,y0,newX,newY,newX-1,newY, mainBoard);
    	        	try {
    	        		connection.send(rightMoveData);
    	        	}
    	        	catch (Exception e1) {
    	        		chat.appendText("O movimento para a direita não foi registrado.\n");
    	        	}
    				break;
    			case DOWN:
    				piece.move(newX * TILE_SIZE, newY * TILE_SIZE);
    				mainBoard[newX][newY].setPiece(piece);
    				mainBoard[x0][y0].setPiece(null);
    				mainBoard[newX][newY-1].setPiece(null);
    				pieceGroup.getChildren().remove(result.getPiece());
    				player.setIsTurn(false);
    				setPiecesMovement(player.getIsTurn());
    				onGameEnded(checkIfGameEnded());
    				DataToSend downMoveData = new DataToSend(TypeOfDataSent.MOVEDATA,x0,y0,newX,newY,newX,newY-1, mainBoard);
    	        	try {
    	        		connection.send(downMoveData);
    	        	}
    	        	catch (Exception e1) {
    	        		chat.appendText("O movimento para baixo não foi registrado. \n");
    	        	}
    				break;
    			case UP:
    				piece.move(newX * TILE_SIZE, newY * TILE_SIZE);
    				mainBoard[newX][newY].setPiece(piece);
    				mainBoard[x0][y0].setPiece(null);
    				mainBoard[newX][newY+1].setPiece(null);
    				pieceGroup.getChildren().remove(result.getPiece());
    				player.setIsTurn(false);
    				setPiecesMovement(player.getIsTurn());
    				onGameEnded(checkIfGameEnded());
    				DataToSend upMoveData = new DataToSend(TypeOfDataSent.MOVEDATA,x0,y0,newX,newY,newX,newY+1, mainBoard);
    	        	try {
    	        		connection.send(upMoveData);
    	        	}
    	        	catch (Exception e1) {
    	        		chat.appendText("O movimento para cima não foi registrado. \n");
    	        	}
    				break;
    		}
    	});
    	

    	
    	return piece;
    }
    
    // Checar condições para a peça se mover
    private boolean checkIfThePieceCanMove(int x, int y) {
    	
    	
    	//Para a direita
    	if(mainBoard[x+1][y] != null && mainBoard[x+2][y] != null && mainBoard[x+1][y].hasPiece() && !(mainBoard[x+2][y].hasPiece()) && mainBoard[x+2][y].getIsPartOfBoard()) {
    		return true;
    	}
    	
    	//Para a esquerda
    	if(mainBoard[x-1][y] != null && mainBoard[x-2][y] != null && mainBoard[x-1][y].hasPiece() && !(mainBoard[x-2][y].hasPiece()) && mainBoard[x-2][y].getIsPartOfBoard()) {
    		return true;
    	}
    	
    	//Para baixo
    	if(mainBoard[x][y+1] != null && mainBoard[x][y+2] != null && mainBoard[x][y+1].hasPiece() && !(mainBoard[x][y+2].hasPiece()) && mainBoard[x][y+2].getIsPartOfBoard()) {
    		return true;
    	}
    	
    	//Para cima
    	if(mainBoard[x][y-1] != null && mainBoard[x][y-2] != null && mainBoard[x][y-1].hasPiece() && !(mainBoard[x][y-2].hasPiece()) && mainBoard[x][y-2].getIsPartOfBoard()) {
    		return true;
    	}
    	
    	//Não pode mover
    	return false;
    }
    
    private GameResult checkIfGameEnded() {
    	int avaliableMoves = 0;
    	int avaliablePieces = 0;
    	
		// Primeiras duas linhas do tabuleiro
		for(int y = 2; y < 4; y++) {
			for(int x = 10; x < 13; x++ ) {
				if(mainBoard[x][y] != null && mainBoard[x][y].hasPiece()) {
					avaliablePieces += 1;
					if(checkIfThePieceCanMove(x,y)) {
						avaliableMoves += 1;
					}
				}
			}
		}
		
		
		//Linhas 3 a 5 do tabuleiro
		for(int y = 4; y < 7; y++) {
			for(int x = 8; x < 15; x++ ) {
				if(mainBoard[x][y] != null && mainBoard[x][y].hasPiece()) {
					avaliablePieces += 1;
					if(checkIfThePieceCanMove(x,y)) {
						avaliableMoves += 1;
					}
				}
				
			}
		
		}
		
		//Duas últimas linhas do tabuleiro
		for(int y = 7; y < 9; y++) {
			for(int x = 10; x < 13; x++ ) {
				if(mainBoard[x][y] != null && mainBoard[x][y].hasPiece()) {
					avaliablePieces += 1;
					if(checkIfThePieceCanMove(x,y)) {
						avaliableMoves += 1;
					}
				}
			}
		}
    	
    	if(avaliableMoves > 0) {
    		return GameResult.STILLAVALIABLEMOVES;
    	}
    	
    	if(avaliableMoves == 0 && avaliablePieces > 1) {
    		return GameResult.NOMOVESLEFT;
    	}
    	
    	if(avaliablePieces == 1) {
    		return GameResult.NOPIECESLEFT;
    	}
    	
    	return GameResult.STILLAVALIABLEMOVES;
    }
    
    // Habilitar/desabilitar movimentação das peças do tabuleiro
    private void setPiecesMovement(boolean canMove) {
    	for(int y = 2; y < HEIGHT - 2; y++) {
    		for(int x = 8; x < WIDTH - 2; x++) {
    			if(mainBoard[x][y] != null && mainBoard[x][y].hasPiece()) {
    				mainBoard[x][y].getPiece().setCanMove(canMove);
    			}
    		}
    	}
    }
    
    // Ações a serem realizadas no final do jogo
    private void onGameEnded(GameResult gameResult) {
    	if(gameResult.name().equalsIgnoreCase("NOPIECESLEFT")) {
    		setPiecesMovement(false);
    		endGameText.setText("Fim do jogo! \n Não há mais peças disponíveis para mover.");
    		player.setWinner(true);
    		withdrawalButton.setDisable(true);
    		if(isServer) {
    			winImage1.setVisible(true);
    		}
    		else {
    			winImage2.setVisible(true);
    		}
    		DataToSend noPiecesData = new DataToSend(TypeOfDataSent.RESULTDATA, player, GameResult.NOPIECESLEFT);
        	try {
        		connection.send(noPiecesData);
        	}
        	catch (Exception e1) {
        		chat.appendText("Não foi possível enviar o resultado do jogo. \n");
        	}
    	}
    	
    	if(gameResult.name().equalsIgnoreCase("NOMOVESLEFT")) {
    		setPiecesMovement(false);
    		endGameText.setText("Fim do jogo! \n Não há mais movimentos disponíveis.");
    		player.setWinner(false);
    		withdrawalButton.setDisable(true);
    		if(isServer) {
    			winImage2.setVisible(true);
    		}
    		else {
    			winImage1.setVisible(true);
    		}
    		DataToSend noMovesData = new DataToSend(TypeOfDataSent.RESULTDATA, player, GameResult.NOMOVESLEFT);
        	try {
        		connection.send(noMovesData);
        	}
        	catch (Exception e1) {
        		chat.appendText("Não foi possível enviar o resultado do jogo. \n");
        	}
    	}
    	
    	if(gameResult.name().equalsIgnoreCase("PLAYERGAVEUP")) {
    		setPiecesMovement(false);
    		player.setWinner(false);
    		String endText = String.format("Fim do jogo! \n Jogador %s desistiu!", player.getName());
    		endGameText.setText(endText);
    		if(isServer) {
    			winImage2.setVisible(true);
    		}
    		else {
    			winImage1.setVisible(true);
    		}
    		DataToSend gaveUpData = new DataToSend(TypeOfDataSent.RESULTDATA, player, GameResult.PLAYERGAVEUP);
        	try {
        		connection.send(gaveUpData);
        	}
        	catch (Exception e1) {
        		chat.appendText("Não foi possível enviar o resultado do jogo. \n");
        	}
    	}
    	
    }
    
    @Override
    public void init() throws Exception{
    	connection.startConnection();
    }
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Scene loginScene = new Scene(createLogin());
			primaryStage.setTitle("Resta Um");;
			primaryStage.setScene(loginScene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws Exception {
		connection.closeConnection();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
