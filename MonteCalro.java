import java.util.Random;
import java.util.Vector;

abstract class AI3
{
	public abstract void move(Board board);
}

class MonteCalroAI extends AI3 implements Cloneable
{
	class Move extends Point
	{

		public Move()
		{
			super(0, 0);
		}

		public Move(int x, int y, int e)
		{
			super(x, y);
		}
	}
	Disc stone[] = new Disc[60];
	Vector can_put = new Vector();
	int try_num = 1,all_playouts = 0;
	Board state = new Board();//Boardクラスのオブジェクト作成
	Board clone = state.clone();//Boardクラスのクローン作製
	ConsoleBoard board = new ConsoleBoard();
	Disc disc = new Disc();
	int difference[] = new int[5];

	public void move(Board board)//打てる手が複数あればモンテカルロ木探索を行う
	{
		can_put = board.getMovablePos();
		BookManager book = new BookManager();
		Vector movables = book.find(board);
		if(movables.isEmpty())
		{
			board.pass();
			return;
		}
		Point p = null;
		if(movables.size() == 1)
		{
			board.move((Point) movables.get(0));
			return;
		}
		if(movables.size() >= 2)
		{
			board.move(select_best_move(board,can_put));
		}
		return;
	}

	public Point select_best_move(Board board, Vector can_put2)//打てる手に対して勝率の
	{				 										   //一番高い手を返す
		BookManager book = new BookManager();
		Vector movables = book.find(board);
		int win_sum[] = new int[can_put2.size()];
		int win = 0;
		Point p = null;
		int best_put = 0;
		double best_value = -100;
		double win_rate[] = new double[can_put2.size()];
	/*	Vector z = board.getUpdate();
		System.out.println(z);*/
		for(;;)
		{
			for(int j = 0; j < can_put2.size(); j++)
			{
				stone[j] = (Disc)can_put2.get(j);
				System.out.print(stone[j] + ",");
			}
			System.out.println("");
			for(int i = 0; i < can_put2.size(); i++)
			{
				for(int m = 0; m < try_num; m++)//プレイアウトを繰り返す
				{
					win = playout(board,m,stone[i]);
					win_sum[i] += win;
				}
				win_rate[i] = (double)win_sum[i] / try_num;//勝率を求める
				System.out.print(", win_sum = " + win_sum[i] + ", win_rate = " + win_rate[i]);
			}
			for(int l = 0; l < can_put2.size(); l++)
			{
				if(win_rate[l] > best_value)//最善手を更新
				{
					best_value = win_rate[l];
					best_put = l;
				}
			}
			break;
		}
		System.out.println();
		System.out.println("(x,y) = " + stone[best_put] + ", best_value = " + best_value + ", try_num = " + try_num);
		return stone[best_put];
	}

	public int playout(Board board, int num, Point put)//プレイアウトをランダムに行う
	{
		BookManager book = new BookManager();
		Vector movables = book.find(clone);
		clone.Turns = 0;
		//System.out.print(clone.getCurrentColor());
		all_playouts++;
		//System.out.println();
		for(int d = 1; d < 9; d++)
		{
			for(int e = 1; e < 9; e++)
			{
				clone.RawBoard[e][d] = board.RawBoard[e][d];
				//System.out.printf("%2d ",clone.RawBoard[e][d]);
			}
			//System.out.println();
		}
		int win = 0;
		System.out.println("");
		int number = 0;
		System.out.print(put + ", ");
		//clone.move(put);
		/*while(clone.Turns == 0)
		{
			System.out.print("HHH");
			clone.move(put);
		}*/
		clone.move(put);
		while(!clone.isGameOver())
		{
			/*System.out.printf("%2d ",clone.getCurrentColor());
			if((all_playouts > 4)&&(number < 10))
			{
				System.out.println();
				for(int d = 0; d < 10; d++)
				{
					for(int e = 0; e < 10; e++)
					{
						System.out.printf("%2d ",clone.RawBoard[e][d]);
					}
					System.out.println();
				}
			}*/
			number++;
			if(movables.isEmpty())
			{
				clone.pass();
			}
			if(movables.size() == 1)
			{
				clone.move((Point) movables.get(0));
			/*	if((all_playouts > 4)&&(number < 10))
				{
					System.out.print((Point) movables.get(0) + ",");
				}*/
			}
			Point p = null;
			if(movables.size() >= 2)
			{
				int x = 0,y = 0;
				disc.color = clone.getCurrentColor();
				for(;;)
				{
					Random value1 = new Random();
					Random value2 = new Random();
					disc.x = value1.nextInt(8) + 1;
					disc.y = value2.nextInt(8) + 1;
					if(clone.checkMobility(disc) == clone.NONE)	continue;//打てる手が無ければ次の座標を調べる(多分これで良い)
					clone.move(disc);
					/*if((all_playouts > 4)&&(number < 10))
					{
						System.out.print(disc + ",");
					}*/
					break;
				}
			}
		}
		System.out.println();
		System.out.print("黒:" + clone.countDisc(Disc.BLACK) + "白:" + clone.countDisc(Disc.WHITE) + " ");
		difference[num] = (clone.countDisc(Disc.BLACK) + 2) - (clone.countDisc(Disc.WHITE) + 2);
		System.out.print("石差 = " + difference[num] + ", " + "手数計 = " + number);
		if(difference[num] > 0)//黒が勝っていれば1
		{
			if((number % 2 == 1)&&(number < 60))//本来の手数でなければ無視
			{
				win = 1;
			}
		}
		return win;
	}
}