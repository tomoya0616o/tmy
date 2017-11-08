class ColorStorage//石の数を保存しておく(白、黒)
{
	private int data[] = new int[3];
	public int get(int color)
	{
		return data[color+1];
	}

	public void set(int color, int value)
	{
		data[color+1] = value;
	}
}
