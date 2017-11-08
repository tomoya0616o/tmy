import java.util.Random;
import java.util.Vector;


abstract class AI2
{
	public abstract void move(Board board);
}

class RandomAI extends AI2 {
	Disc z[] = new Disc[60];
	Vector x = new Vector();
	Vector y = new Vector();

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

	public void move(Board board)
	{
		/*System.out.println();
		for(int d = 0; d < 10; d++)
		{
			for(int e = 0; e < 10; e++)
			{
				System.out.printf("%2d ",board.RawBoard[e][d]);
			}
			System.out.println();
		}
		x = board.getUpdate();
		System.out.println(x);*/
		BookManager book = new BookManager();
		Vector movables = book.find(board);
		if(movables.isEmpty())
		{
			// 打てる箇所がなければパスする
			board.pass();
			return;
		}
		Point p = null;
		if(movables.size() == 1)
		{
			// 打てる箇所が一カ所だけなら、即座に打って返る
			board.move((Point) movables.get(0));
			return;
		}
		//Randomに打つ
		if(movables.size() >= 2)
		{
			y = board.getMovablePos();
			for(int i = 0; i < y.size(); i++)
			{
				z[i] = (Disc) y.get(i);
			}
			int x = 0;
			Random value1 = new Random();
			x = value1.nextInt(y.size());
			System.out.println(z[x]);
			board.move((Point)z[x]);
			return;
		}
	}
}