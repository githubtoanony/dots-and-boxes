package net.za.coldshift;

import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;

import android.util.Log;

public class LineModel extends CCNode {
	private CCSprite sprite;
	
	public DotModel dot;
	public DotModel lastDot;
	
	public LineModel()
	{
	}
	
	/**
	 * create a new line
	 * @param point1
	 * @param point2
	 * @param sprite
	 */
	public LineModel(DotModel dot, DotModel lastDot, String spriteImage)
	{
		this.dot = dot;
		this.lastDot = lastDot;
		
		// load default sprite
		sprite = CCSprite.sprite(spriteImage + "_line_vertical.png");
		
		// get sprite dimensions
		int spriteWidth = (int) sprite.getBoundingBox().size.width;
		int spriteHeight = (int) sprite.getBoundingBox().size.height;
		
		// scale sprite to create a line
		
		if(dot.center.x != lastDot.center.x && dot.center.y != lastDot.center.y)
		{
			// diagonal
			
			sprite = CCSprite.sprite(spriteImage + ".png");
			sprite.setPosition(lastDot.center.x + ((dot.center.x - lastDot.center.x) / 2), 
							   lastDot.center.y + ((dot.center.y - lastDot.center.y) / 2));
			
			// do some trig to determine angle and length of hypotenuse
			float z = 0;

			//    
			//   |\    
			// y |  \  z
			//   |    \
			//   +------
			//     x 
			// 
			
			float x = Math.abs(dot.center.x - lastDot.center.x) / spriteWidth;
			float y = Math.abs(dot.center.y - lastDot.center.y) / spriteHeight;
			
			// phythagorean theorem 
			z = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
			
			// soh ca toa -> sin(theta) = opp / hyp = y / z, so
			// (math.asin returns value in radians)
			float theta = (float) Math.toDegrees((Math.asin(y / z)));
			
			if(dot.center.x < lastDot.center.x && dot.center.y < lastDot.center.y)
			{
				// invert angle
				theta = -theta;
			}
			else if(dot.center.x > lastDot.center.x && dot.center.y > lastDot.center.y)
			{
				// invert angle
				theta = -theta;
			}
				
			sprite.setScaleX(z);
			sprite.setRotation(theta);
		}
		else if(dot.center.x != lastDot.center.x)
		{
			// x
			
			sprite = CCSprite.sprite(spriteImage + "_line_horizontal.png");
			sprite.setPosition(lastDot.center.x + ((dot.center.x - lastDot.center.x) / 2), lastDot.center.y);
			sprite.setScaleX(Math.abs(dot.center.x - lastDot.center.x) / spriteWidth);
		}
		else if(dot.center.y != lastDot.center.y)
		{
			// y
			
			sprite = CCSprite.sprite(spriteImage + "_line_vertical.png");
			sprite.setPosition(lastDot.center.x, lastDot.center.y + ((dot.center.y - lastDot.center.y) / 2));
			sprite.setScaleY(Math.abs(dot.center.y - lastDot.center.y) / spriteHeight);
		}
		
		addChild(sprite);
	}
}
