import java.util.Arrays;
import java.util.Vector;

class Board implements Cloneable
{
	public static final int BOARD_SIZE =  8;
	public static final int MAX_TURNS  = 60;

	protected static final int NONE		 =   0;
	private static final int UPPER		 =   1;
	private static final int UPPER_LEFT	 =   2;
	private static final int LEFT		 =   4;
	private static final int LOWER_LEFT	 =   8;
	private static final int LOWER		 =  16;
	private static final int LOWER_RIGHT =  32;
	private static final int RIGHT		 =  64;
	private static final int UPPER_RIGHT = 128;

	protected int RawBoard[][] = new int[BOARD_SIZE+2][BOARD_SIZE+2];
	int Turns; // 手数(0からはじまる)
	protected int CurrentColor; // 現在のプレイヤー

	public Vector UpdateLog = new Vector();

	//打てる手を格納
	protected Vector MovablePos[] = new Vector[MAX_TURNS+1];

	//checkMobilityで得た情報を格納
	protected int MovableDir[][][] = new int[MAX_TURNS+1][BOARD_SIZE+2][BOARD_SIZE+2];

	protected ColorStorage Discs = new ColorStorage();

	//cloneにプレイアウトを割り当てる,Boardの盤面をコピー
	@Override
	public Board clone()
	{
		try
		{
			//Boardクラス、フィールドのコピー
			Board clone = (Board)super.clone();
			ConsoleBoard board = new ConsoleBoard();
			clone.Discs = new ColorStorage();
			clone.RawBoard = Arrays.copyOf(RawBoard, RawBoard.length);
			clone.UpdateLog = new Vector();
			clone.MovablePos = Arrays.copyOf(MovablePos, MovablePos.length);
			clone.MovableDir = Arrays.copyOf(MovableDir, MovableDir.length);
			return clone;
		}
		catch(CloneNotSupportedException e)
		{
			throw new InternalError(e.toString());
		}
	}

	public Board()
	{
		// Vectorの配列を初期化
		for(int i=0; i<=MAX_TURNS; i++)
		{
			MovablePos[i] = new Vector();
		}
		init();
	}

	public void init()
	{
		// 全マスを空きマスに設定
		for(int x=1; x <= BOARD_SIZE; x++)
		{
			for(int y=1; y <= BOARD_SIZE; y++)
			{
				RawBoard[x][y] = Disc.EMPTY;
			}
		}

		// 壁の設定
		for(int y=0; y < BOARD_SIZE + 2; y++)
		{
			RawBoard[0][y] = Disc.WALL;
			RawBoard[BOARD_SIZE+1][y] = Disc.WALL;
		}

		for(int x=0; x < BOARD_SIZE + 2; x++)
		{
			RawBoard[x][0] = Disc.WALL;
			RawBoard[x][BOARD_SIZE+1] = Disc.WALL;
		}


		// 初期配置
		RawBoard[4][4] = Disc.WHITE;
		RawBoard[5][5] = Disc.WHITE;
		RawBoard[4][5] = Disc.BLACK;
		RawBoard[5][4] = Disc.BLACK;

		// 石数の初期設定
		Discs.set(Disc.BLACK, 2);
		Discs.set(Disc.WHITE, 2);
		Discs.set(Disc.EMPTY, BOARD_SIZE*BOARD_SIZE - 4);

		Turns = 0; // 手数は0から数える
		CurrentColor = Disc.BLACK; // 先手は黒

		initMovable();
	}

	public boolean move(Point point)
	{
		if(point.x <= 0 || point.x > BOARD_SIZE) return false;
		if(point.y <= 0 || point.y > BOARD_SIZE) return false;
		if(MovableDir[Turns][point.x][point.y] == NONE) return false;

		flipDiscs(point);

		Turns++;
		CurrentColor = -CurrentColor;

		initMovable();

		return true;
	}

	public boolean undo()
	{
		// ゲーム開始地点ならもう戻れない
		if(Turns == 0) return false;

		CurrentColor = -CurrentColor;

		Vector update = (Vector) UpdateLog.remove(UpdateLog.size()-1);

		// 前回がパスかどうかで場合分け
		if(update.isEmpty())
		{
			// 前回はパス

			// MovablePos及びMovableDirを再構築
			MovablePos[Turns].clear();
			for(int x=1; x<=BOARD_SIZE; x++)
			{
				for(int y=1; y<=BOARD_SIZE; y++)
				{
					MovableDir[Turns][x][y] = NONE;
				}
			}
		}
		else
		{
			// 前回はパスでない

			Turns--;

			// 石を元に戻す
			Point p = (Point) update.get(0);
			RawBoard[p.x][p.y] = Disc.EMPTY;
			for(int i=1; i<update.size(); i++)
			{
				p = (Point) update.get(i);
				RawBoard[p.x][p.y] = -CurrentColor;
			}

			// 石数の更新
			int discdiff = update.size();
			Discs.set(CurrentColor, Discs.get(CurrentColor) - discdiff);
			Discs.set(-CurrentColor, Discs.get(-CurrentColor) + (discdiff - 1));
			Discs.set(Disc.EMPTY, Discs.get(Disc.EMPTY) + 1);
		}

		return true;
	}

	public boolean pass()
	{
		// 打つ手があればパスできない
		if(MovablePos[Turns].size() != 0) return false;

		// ゲームが終了しているなら、パスできない
		if(isGameOver()) return false;

		CurrentColor = -CurrentColor;

		UpdateLog.add(new Vector());

		initMovable();

		return true;

	}

	public int getColor(Point point)
	{
		return RawBoard[point.x][point.y];
	}

	public int getCurrentColor()
	{
		return CurrentColor;
	}

	public int getTurns()
	{
		return Turns;
	}

	public boolean isGameOver()
	{
		// 60手に達していたらゲーム終了
		if(Turns == MAX_TURNS) return true;

		// 打てる手があるならゲーム終了ではない
		if(MovablePos[Turns].size() != 0) return false;

		//
		//	現在の手番と逆の色が打てるかどうか調べる
		//
		Disc disc = new Disc();
		disc.color = -CurrentColor;
		for(int x=1; x<=BOARD_SIZE; x++)
		{
			disc.x = x;
			for(int y=1; y<=BOARD_SIZE; y++)
			{
				disc.y = y;
				// 置ける箇所が1つでもあればゲーム終了ではない
				if(checkMobility(disc) != NONE) return false;
			}
		}

		return true;
	}

	public int countDisc(int color)
	{
		return Discs.get(color);
	}

	public Vector getMovablePos()
	{
		return MovablePos[Turns];
	}

	public Vector getHistory()
	{
		Vector history = new Vector();

		for(int i=0; i<UpdateLog.size(); i++)
		{
			Vector update = (Vector) UpdateLog.get(i);
			if(update.isEmpty()) continue; // パスは飛ばす
			history.add(update.get(0));
		}

		return history;
	}

	public Vector getUpdate()
	{
		if(UpdateLog.isEmpty()) return new Vector();
		else return (Vector) UpdateLog.lastElement();
	}

	public int getLiberty(Point p)
	{
		// 仮
		return 0;
	}

	int checkMobility(Disc disc)
	{
		// 既に石があったら置けない
		if(RawBoard[disc.x][disc.y] != Disc.EMPTY) return NONE;

		int x, y;
		int dir = NONE;

		// 上
		if(RawBoard[disc.x][disc.y-1] == -disc.color)
		{
			x = disc.x; y = disc.y-2;
			while(RawBoard[x][y] == -disc.color) { y--; }
			if(RawBoard[x][y] == disc.color) dir |= UPPER;
		}

		// 下
		if(RawBoard[disc.x][disc.y+1] == -disc.color)
		{
			x = disc.x; y = disc.y+2;
			while(RawBoard[x][y] == -disc.color) { y++; }
			if(RawBoard[x][y] == disc.color) dir |= LOWER;
		}

		// 左
		if(RawBoard[disc.x-1][disc.y] == -disc.color)
		{
			x = disc.x-2; y = disc.y;
			while(RawBoard[x][y] == -disc.color) { x--; }
			if(RawBoard[x][y] == disc.color) dir |= LEFT;
		}

		// 右
		if(RawBoard[disc.x+1][disc.y] == -disc.color)
		{
			x = disc.x+2; y = disc.y;
			while(RawBoard[x][y] == -disc.color) { x++; }
			if(RawBoard[x][y] == disc.color) dir |= RIGHT;
		}


		// 右上
		if(RawBoard[disc.x+1][disc.y-1] == -disc.color)
		{
			x = disc.x+2; y = disc.y-2;
			while(RawBoard[x][y] == -disc.color) { x++; y--; }
			if(RawBoard[x][y] == disc.color) dir |= UPPER_RIGHT;
		}

		// 左上
		if(RawBoard[disc.x-1][disc.y-1] == -disc.color)
		{
			x = disc.x-2; y = disc.y-2;
			while(RawBoard[x][y] == -disc.color) { x--; y--; }
			if(RawBoard[x][y] == disc.color) dir |= UPPER_LEFT;
		}

		// 左下
		if(RawBoard[disc.x-1][disc.y+1] == -disc.color)
		{
			x = disc.x-2; y = disc.y+2;
			while(RawBoard[x][y] == -disc.color) { x--; y++; }
			if(RawBoard[x][y] == disc.color) dir |= LOWER_LEFT;
		}

		// 右下
		if(RawBoard[disc.x+1][disc.y+1] == -disc.color)
		{
			x = disc.x+2; y = disc.y+2;
			while(RawBoard[x][y] == -disc.color) { x++; y++; }
			if(RawBoard[x][y] == disc.color) dir |= LOWER_RIGHT;
		}

		return dir;
	}

	// MovableDir及びMovablePosを初期化する
	private void initMovable()
	{
		Disc disc;
		int dir;

		MovablePos[Turns].clear();

		for(int x=1; x<=BOARD_SIZE; x++)
		{
			for(int y=1; y<=BOARD_SIZE; y++)
			{
				disc = new Disc(x, y, CurrentColor);
				dir = checkMobility(disc);
				if(dir != NONE)
				{
					// 置ける
					MovablePos[Turns].add(disc);
				}
				MovableDir[Turns][x][y] = dir;
			}
		}
	}

	private void flipDiscs(Point point)
	{
		int x, y;
		int dir = MovableDir[Turns][point.x][point.y];

		Vector update = new Vector();

		RawBoard[point.x][point.y] = CurrentColor;
		update.add(new Disc(point.x, point.y, CurrentColor));


		// 上

		if((dir & UPPER) != NONE) // 上に置ける
		{
			y = point.y;
			while(RawBoard[point.x][--y] != CurrentColor)
			{
				RawBoard[point.x][y] = CurrentColor;
				update.add(new Disc(point.x, y, CurrentColor));
			}
		}


		// 下

		if((dir & LOWER) != NONE)
		{
			y = point.y;
			while(RawBoard[point.x][++y] != CurrentColor)
			{
				RawBoard[point.x][y] = CurrentColor;
				update.add(new Disc(point.x, y, CurrentColor));
			}
		}

		// 左

		if((dir & LEFT) != NONE)
		{
			x = point.x;
			while(RawBoard[--x][point.y] != CurrentColor)
			{
				RawBoard[x][point.y] = CurrentColor;
				update.add(new Disc(x, point.y, CurrentColor));
			}
		}

		// 右

		if((dir & RIGHT) != NONE)
		{
			x = point.x;
			while(RawBoard[++x][point.y] != CurrentColor)
			{
				RawBoard[x][point.y] = CurrentColor;
				update.add(new Disc(x, point.y, CurrentColor));
			}
		}

		// 右上

		if((dir & UPPER_RIGHT) != NONE)
		{
			x = point.x;
			y = point.y;
			while(RawBoard[++x][--y] != CurrentColor)
			{
				RawBoard[x][y] = CurrentColor;
				update.add(new Disc(x, y, CurrentColor));
			}
		}

		// 左上

		if((dir & UPPER_LEFT) != NONE)
		{
			x = point.x;
			y = point.y;
			while(RawBoard[--x][--y] != CurrentColor)
			{
				RawBoard[x][y] = CurrentColor;
				update.add(new Disc(x, y, CurrentColor));
			}
		}

		// 左下

		if((dir & LOWER_LEFT) != NONE)
		{
			x = point.x;
			y = point.y;
			while(RawBoard[--x][++y] != CurrentColor)
			{
				RawBoard[x][y] = CurrentColor;
				update.add(new Disc(x, y, CurrentColor));
			}
		}

		// 右下

		if((dir & LOWER_RIGHT) != NONE)
		{
			x = point.x;
			y = point.y;
			while(RawBoard[++x][++y] != CurrentColor)
			{
				RawBoard[x][y] = CurrentColor;
				update.add(new Disc(x, y, CurrentColor));
			}
		}

		// 石の数を更新

		int discdiff = update.size();

		Discs.set(CurrentColor, Discs.get(CurrentColor) + discdiff);
		Discs.set(-CurrentColor, Discs.get(-CurrentColor) - (discdiff - 1));
		Discs.set(Disc.EMPTY, Discs.get(Disc.EMPTY) - 1);

		UpdateLog.add(update);
	}
}