import java.io.BufferedReader;
import java.io.InputStreamReader;

interface Player
{
	public void onTurn(Board board) throws Exception;
}

class UndoException extends Exception
{
}

class ExitException extends Exception
{
}

class GameOverException extends Exception
{
}

class HumanPlayer implements Player
{
	public void onTurn(Board board) throws Exception
	{
		if(board.getMovablePos().isEmpty())
		{
			// パス
			System.out.println("あなたはパスです。");
			board.pass();
			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while(true)
		{
			System.out.print("手を\"f5\"のように入力、もしくは(U:取消/X:終了)を入力してください:");
			String in = br.readLine();

			if(in.equalsIgnoreCase("U")) throw new UndoException();

			if(in.equalsIgnoreCase("X")) throw new ExitException();

			Point p;
			try{
				p = new Point(in);
			}
			catch(IllegalArgumentException e)
			{
				System.out.println("正しい形式で入力してください！");
				continue;
			}

			if(!board.move(p))
			{
				System.out.println("そこには置けません！");
				continue;
			}

			if(board.isGameOver()) throw new GameOverException();

			break;
		}
	}
}

//αβ法
class AIPlayer1 implements Player
{

	private AI Ai1 = null;

	public AIPlayer1()
	{
		Ai1 = new AlphaBetaAI();
	}

	public void onTurn(Board board) throws GameOverException
	{
		System.out.print("コンピュータが思考中...");
		Ai1.move(board);
		System.out.println("完了");
		if(board.isGameOver()) throw new GameOverException();
	}
}
//MonteCalro法
class AIPlayer2 implements Player
{
	private MonteCalroAI Ai2 = null;

	public AIPlayer2()
	{
		Ai2 = new MonteCalroAI();
	}

	public void onTurn(Board board) throws GameOverException
	{
		System.out.print("コンピュータが思考中...");
		Ai2.move(board);
		System.out.println("完了");
		if(board.isGameOver()) throw new GameOverException();
	}
}
//Random
class AIPlayer3 implements Player
{
	private RandomAI Ai3 = null;

	public AIPlayer3()
	{
		Ai3 = new RandomAI();
	}

	public void onTurn(Board board) throws GameOverException
	{
		System.out.print("コンピュータが思考中...");
		Ai3.move(board);
		System.out.println("完了");
		if(board.isGameOver()) throw new GameOverException();
	}
}

class ReversiGame
{
	public static void main(String[] args)
	{
		Player[] player = new Player[2];
		int current_player = 0;
		ConsoleBoard board = new ConsoleBoard();
		boolean reverse = true;

		if(args.length > 0)
		{
			// コマンドラインオプション -r が与えられるとコンピュータ先手にする
			if(args[0].equals("-r")) reverse = true;
		}

		// 先手・後手の設定
		if(reverse)
		{
			player[0] = new AIPlayer2();
			player[1] = new AIPlayer3();
		}
		else
		{
			player[0] = new AIPlayer3();
			player[1] = new AIPlayer3();
		}

		while(true)
		{
			board.print();

			try{
				player[current_player].onTurn(board);
			}
			catch(UndoException e)
			{
				do
				{
					board.undo(); board.undo();
				} while(board.getMovablePos().isEmpty());
				continue;
			}
			catch(ExitException e)
			{
				return;
			}
			catch(GameOverException e)
			{
				System.out.println("ゲーム終了");
				System.out.print("黒石" + board.countDisc(Disc.BLACK) + " ");
				System.out.println("白石" + board.countDisc(Disc.WHITE));

				return;
			}
			catch(Exception e)
			{
				// 予期しない例外
				System.out.println("Unexpected exception: " + e);
				return;
			}

			// プレイヤーの交代
			current_player = ++current_player % 2;
		}

	}
}
