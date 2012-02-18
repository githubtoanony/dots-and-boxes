package net.za.coldshift;

import java.util.ArrayList;

import android.util.Log;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;

import android.view.MotionEvent;

public class GameLayer extends CCColorLayer {
	
	private ArrayList<Object> dots = new ArrayList<Object>();
	private int totalShapes = 0;
	
	private ArrayList<Player> players = new ArrayList<Player>();
	private int currentPlayer = 0;
	
	private String lastDotId = "";
	
	private int headerHeight = 100;
	private int padding = 20;
	private int move = 0;
	private int matrix = 2;
	
	private boolean gameOver = false;

	public static boolean allowDiagonal = false;
	
	public static CGSize windowSize;
	
	private CCLabel winLabel;
	
	public static CCScene scene()
	{
	    CCScene scene = CCScene.node();
	    CCColorLayer layer = new GameLayer(ccColor4B.ccc4(0, 0, 0, 255));
	    scene.addChild(layer);
	    
	    return scene;
	}	
	
	protected GameLayer(ccColor4B color)
	{
		super(color);
		
		// enable touch
		this.isTouchEnabled_= true;
		
		windowSize = CCDirector.sharedDirector().displaySize();
	    
	    start(matrix);
	}

	private void start(int matrix)
	{
		// reset scene
		dots.clear();
		players.clear();
		currentPlayer = 0;
		lastDotId = "";
		move = 0;
		
		gameOver = false;
		
	    // create our users
	    players.add(new Player("player 1", ccColor4B.ccc4(0, 0, 255, 255)));	// blue
	    players.add(new Player("player 2", ccColor4B.ccc4(0, 255, 0, 255)));	// green
		
	    removeAllChildren(true);
	    
	    // get width minus padding
	    int width = (int) (windowSize.getWidth() - (2 * padding));
	    
	    // get height minus header and padding
	    int height = (int)windowSize.getHeight() - (2 * padding) - 100;
	     
	    // calculate margin between dots
	    int marginX = width / matrix;
	    int marginY = height / matrix;
	    
    	int x, y;
	
    	// magic part 1
    	// add each dot, including a border of dummy dots 
    	// dummy dots will be used to determine the shape in the neighborhood generator
	    for(int i = -1; i < matrix + 1; i++)
	    { 
	    	// calculate Y offset 
	    	y = padding + (i * marginY);
	    	
		    for(int j = -1; j < matrix + 1; j++)
		    {
		    	// calculate X offset 
		    	x = padding + (j * marginX);
		    	
		    	if((i >= 0 && i < matrix) && 
      			   (j >= 0 && j < matrix)
		    	  )
		    	{
			    	// create dot object
			    	DotModel dotModel = new DotModel("dot_" + j + "_" + i,
			    									 x, 
			    									 y,
			    									 marginX,
			    									 marginY
			    									 );
				    	
			    	dots.add(dotModel);
			    	addChild(dotModel);
		    	}
		    	else
		    	{
		    		DummyModel dummyModel = new DummyModel();
		    		dots.add(dummyModel);
		    	}
		    }
	    }
	    
	    // magic part2 
	    generateNeighborhood(matrix + 2, marginX, marginY);  
	    
	    // set total number of shapes that need to be completed
	    totalShapes = (int) Math.pow(matrix - 1, 2);
	    
	    Log.d("battlespirits", "totalshapes: " + totalShapes);
	    		
	}
	
	/**
	 * magic part 2 - generate shapes
	 * @param offset
	 */
	private void generateNeighborhood(int offset, int marginX, int marginY)
	{
	    // now that we have all our dots we need to generate the list of neighbors
	    // ideally this should be part of a level generator
	    for(int i = 0; i < dots.size(); i++)
	    {
	    	// get our current point
	    	Object dotCheck = dots.get(i);

	    	if(dotCheck instanceof DotModel)
	    	{
	    		DotModel dot = (DotModel)dotCheck;
	    		
		    	//
		    	// go with a straight square model for now
		    	// which means that a point model such as
		    	//
		    	// 7 8 9
		    	// 4 5 6
		    	// 1 2 3
		    	//
		    	// in which "5" is the point in question, will have the following relationship with other dots
		    	//
		    	// 1 (i - offset - 1)
		    	// 2 (i - offset)
		    	// 3 (i - offset + 1)
		    	// 4 (i - 1)
		    	// 5 (i)
		    	// 6 (i + 1)
		    	// 7 (i + offset - 1)
		    	// 8 (i + offset)
		    	// 9 (i + offset + 1)
		    	//
		    	// 1, 2, 4, 5 forms boxA
		    	// 2, 3, 5, 6 forms boxB
		    	// 4, 5, 7, 8 forms boxC
		    	// 5, 6, 8, 9 forms boxD
		    	//
		    	
		    	int neighborIndices[] = {(i - offset - 1),
	 	                                 (i - offset),
	 	                                 (i - offset + 1),
		                                 (i - 1), 
		                                 (i),	// just here for completeness
		                                 (i + 1),
		                                 (i + offset - 1),
		                                 (i + offset),
		                                 (i + offset + 1)
							    	    };
	
		    	// our basic shape definition based on the indices above
		    	// note that we can extend this beyond a basic box if desired by just adding more points
		    	
		    	int shapes[][] = {{0, 1, 3, 4},
			  					  {1, 2, 4, 5},
			  					  {3, 4, 6, 7},
			  					  {4, 5, 7, 8}
		    					 };

		    	// check which shapes exist and create them
		    	for(int j = 0; j < shapes.length; j++)
		    	{
		    		if(shapes[j] != null)
		    		{
		    			// create a new shape
		    			ShapeModel shape = new ShapeModel();
		    			
		    			// add necessary dots to the shape
		    			boolean hasDummy = false;
			    		String shapeId = "shape-" + j ;
			    		for(int k = 0; k < shapes[j].length; k++)
			    		{
			    			shapeId += "_" + shapes[j][k];
			    			
			    			Object neighborDot = dots.get((neighborIndices[shapes[j][k]]));
			    			
			    			if(neighborDot instanceof DotModel)
			    			{
			    				shape.dots.add((DotModel)neighborDot);
			    			}
			    			else
			    			{
			    				hasDummy = true;
			    				break;
			    			}
			    		}
			    		
			    		if(!hasDummy)
			    		{
				    		shape.shapeId = shapeId;
				    		shape.areaWH = CGPoint.make(marginX, marginY);
			    		
				    		addChild(shape);
				    		
			    			((DotModel)dot).neighborhood.add(shape);
			    		}
		    		}
	    		}
	    	}
	    }
	}
	
	@Override
	public boolean ccTouchesBegan(MotionEvent event) {
		if(gameOver)
		{
			// restart
			start(++matrix);
			return CCTouchDispatcher.kEventHandled;
		}
		
		// determine what was clicked - need to take padding into account
		CGPoint touchPoint = CGPoint.make(event.getX() - (2 * padding), 
										  windowSize.height - event.getY() + (2 * padding)
										  );	// height is inverted because of coordinate system
		
		if(touchPoint.x > 0 || touchPoint.y > 0)
		{
			for (Object baseDot : dots) 
			{
				if(baseDot instanceof DotModel)
				{
					DotModel dot = (DotModel)baseDot;
					
					if(CGRect.containsPoint(dot.getBoundingBox(), touchPoint))
					{
						// get current player
						Player player = players.get(currentPlayer);
	
						// check dot activation state
						if(!dot.isActivated)
						{
							// check if there was another dot selected
							if(!lastDotId.equals(""))
							{
								// check if dot is next to lastDot
								DotModel lastDot = dot.getNeighbor(lastDotId);
								
								if(lastDot != null)
								{
									// activate dot
									activateDot(player, dot);
									
									// draw a line between the two dots
									drawLine(dot, lastDot, player);
									return CCTouchDispatcher.kEventHandled;
								}
								else
								{
									// invalid move
									return CCTouchDispatcher.kEventHandled;
								}
							}
							else
							{
								// new dot - activate
								activateDot(player, dot);
								return CCTouchDispatcher.kEventHandled;
							}
						}
						else
						{
							// check if user clicked the same dot twice in a row
							if(lastDotId.equals(dot.dotId) && dot.selectedBy == player && dot.move == move)
							{
								// deselect dot
								deactivateDot(dot);
								return CCTouchDispatcher.kEventHandled;
							}
							
							// check if lastDot is selected and it's a neighbor
							if(!lastDotId.equals(""))
							{
								DotModel lastDot = dot.getNeighbor(lastDotId);
								 
								if(lastDot != null && !dot.lineExists(lastDot))
								{ 
									// valid move
									activateDot(player, dot);
									
									// draw a line between the two dots
									drawLine(dot, lastDot, player);
									return CCTouchDispatcher.kEventHandled;
								}
								
								return CCTouchDispatcher.kEventHandled;
							}
							
							// check if any neighbors are still open
							if(dot.hasOpenNeigbors())
							{
								// valid move
								activateDot(player, dot);
								
								return CCTouchDispatcher.kEventHandled;
							}
							
							// invalid move
							return CCTouchDispatcher.kEventHandled;
						}
					}
				}
			}
		}

        return CCTouchDispatcher.kEventHandled;
    }	

	private void drawLine(DotModel dot, DotModel lastDot, Player player)
	{
		LineModel lineModel = new LineModel(dot, lastDot, player.spriteImage);
		addChild(lineModel);
		
		dot.commitAndActivate(lineModel);
		lastDot.commitAndActivate(lineModel);
		
		// score
		score(player, dot);
	}
	
	private void score(Player player, DotModel dot)
	{
		// see if we have any completed shapes around this dot
		ArrayList<ShapeModel> completedShapes = dot.check();
		
		if(completedShapes != null)
		{
			Log.d("battlespirits", "shape completed!");
			for (ShapeModel shape : completedShapes) 
			{
				// shapes completed, so score for whoever had the last turn..
				player.addShape(shape);
				
				// update score
				
				// check if all shapes have been completed
				totalShapes--;
				
				nextRound(true);
			}
		}
		else
		{
			nextRound(false);
		}
		
	}
	
	private void activateDot(Player player, DotModel dot)
	{
		dot.activate(player, ++move);
		lastDotId = new String(dot.dotId);
	}

	private void deactivateDot(DotModel dot)
	{
		dot.deactivate();
		lastDotId = "";
	}
	
	private void nextRound(boolean extraTurn)
	{
		Log.d("battlespirits", "Remaining: " + totalShapes);
		
		// next round
		lastDotId = "";
		
		if(totalShapes == 0)
		{
			// game over?
			determineWinner();
			return;
		}
	
		if(!extraTurn)
		{
			// show current player
			if(currentPlayer < players.size() - 1)
			{
				currentPlayer++;
			}
			else
			{
				currentPlayer = 0;
			}
		}
	}
	
	private Player determineWinner()
	{
		if(players.size() > 0)
		{
			int highScore = 0;
			Player winner = null;
			
			for (Player player : players) {
				Log.i("battlespirits", "Final score: " + player.name + " " + player.shapes.size());
				
				if(player.shapes.size() > highScore)
				{
					winner = player;
					highScore = player.shapes.size();
				}
			}

			Log.i("battlespirits", "GAME OVER: " + winner.name + " WINS");
			
			winLabel = CCLabel.makeLabel( winner.name + " WINS with " + highScore + " point(s)", "Schwarzwald Regular.ttf", 20);
			winLabel.setColor(ccColor3B.ccc3(winner.color.r, winner.color.g, winner.color.b));
			winLabel.setPosition(windowSize.getWidth() / 2, windowSize.getHeight() - 40);
			
			addChild(winLabel);
			gameOver = true;
			return winner;
		}
		
		return null;
	}
	
	@Override
    public boolean ccTouchesEnded(MotionEvent event) {
        return CCTouchDispatcher.kEventIgnored;
    }	
}
