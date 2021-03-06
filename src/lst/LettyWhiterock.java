package lst;
import robocode.*;

import java.awt.Color;

/**
 * LettyWhiterock - a robot by Li Shing To
 * SYS-LYWR-1
 * Description:
 * A guard robot that moves up and down a wall and fires at anything it sees
 * with scanning
 */
public class LettyWhiterock extends AdvancedRobot
{
	/**
	 * run: Cirno's default behavior
	 */
	
	private double shortSide;
	private boolean widthShort;
	private boolean attaching = false;
	private boolean attachingFail = false;
	
	private double turnTarget; 
	private int direction = -1;
	private double turretHeading;
	
	private double scanAlt = 0;
	
	@Override
	public void run() {
		setColors(Color.WHITE,Color.BLUE, Color.white );		
		setBulletColor(Color.CYAN);		
		this.setScanColor(Color.cyan);
		
		double width = this.getBattleFieldWidth();
		double height = this.getBattleFieldHeight();
		
		widthShort = width<height?true:false;
		shortSide = widthShort?width:height;
		wallAttach();
		
		while(true){
			execute();
			scanAlt = 0;
			if(!attaching && !attachingFail){
				//Up and Down
				setAhead(Double.POSITIVE_INFINITY * direction);
			}else if(attachingFail){
				wallAttach();
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {	
		if(!attaching){
			if(e.getVelocity() < 3){
				setAhead(5*direction);
				scanAlt++;
				if(scanAlt > 2){
					direction *= -1;
					scanAlt = 0;
				}
			}
			fire(3);
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		if(!attaching)
			angry(e.getBearing());
		else{
			attaching = false;
			attachingFail = true;
		}
	}
	
	@Override
	public void onHitWall(HitWallEvent e){
		if(attaching){
			double turnTarget = widthShort ? 90 : 0;
			double turnAmt = turnTarget - this.getHeading();
			turnRight(turnAmt);
			
			if(widthShort){
				setTurnGunRight(-90);
			}
			
			turretHeading = getGunHeading();			
			
			attaching = false;
			attachingFail = false;
		}else{
			double gh = getGunHeading();
			if(gh != turretHeading){
				setTurnGunRight(robocode.util.Utils.normalRelativeAngle((turretHeading - gh) * (Math.PI/180)));
			}
			direction *= -1;
		}
	}
	
	@Override
	public void onHitRobot(HitRobotEvent e){
		if(!attaching)
			angry(e.getBearing());
		else{
			attaching = false;
			attachingFail = true;
		}
	}
	
	@Override
	public void onWin(WinEvent e){
		while(true){
			setBulletColor(Color.MAGENTA);
			fire(3);
			setTurnRadarRight(360);
			execute();
		}
	}
	public void angry(double bear){
		double absoluteBearing = this.getHeading() + bear;		
		setTurnGunRight( getGunTurnAmt( absoluteBearing, this.getGunHeading() )	);
		direction *= -1;
	}
	
	public void wallAttach(){
		attaching = true;
		double intHeading = this.getHeading();
		double intGunHeading = this.getGunHeading();
		if(intHeading != intGunHeading)
			turnGunRight(intHeading - intGunHeading);
		
		double sideCoord = widthShort ? getY():getX();
		turnTarget = widthShort ? 0 : 90;
		double turnAmt = turnTarget - intHeading;
		double aheadAmt = sideCoord > shortSide/2 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		setTurnRight(turnAmt);
		setTurnGunRight(sideCoord > shortSide/2?turnTarget-180:turnTarget);
		setAhead(aheadAmt);
	}
	
	public double getGunTurnAmt(double absBear, double gunHeading){
		return getGunTurnAmt(absBear, gunHeading, 1.0);
	}
	
	public double getGunTurnAmt(double absBear, double gunHeading, double factor){
		return Math.toDegrees(
				factor *
				robocode.util.Utils.normalRelativeAngle(
							Math.toRadians(absBear- gunHeading)
						)
					);
	}
	
}
