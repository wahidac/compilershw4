class Test{
    public static void main(String[] a){
	int b;
	int c;
	boolean f;
	boolean g;
	boolean e;
	Jamba j;
	int []arr;
	
	/*g = true && true;
	c = 540;
	j = new Jamba();
	j = (new Tootsie().returnNewJamba()).returnNewJamba();
	j = new Tootsie();
	b = j.yolo(c,g);
	j = new Tootsie();
	//b = j.roko();
	f = true;
	g = true;
	e = f && g;
	c = 3;
	while(!(b < 20)) {
		System.out.println(b);
		b = b - c;
	}*/
	Snookie s;

	b =2;
	if(b < 20) {
		System.out.println(1);
	} else {
		System.out.println(0);
	}
	s = new Snookie();
	b = s.snookieTest();
    }
	
}


class Jamba {
	int f1;
	boolean f2;
	int f3;
	
	public int yolo(int c, boolean b) {
		int ret;
		if(b)
			ret = this.jookie(c, !b);
		else
			ret = 100;
		return ret;
	}
	
	
	public int jookie(int a, boolean b) {
		int ret;
		if(b)
			ret = 999;
		else
			ret = a;
		return ret;
	}
	
	public int roko() {
		return 100;
	}
	
	public Jamba returnNewJamba() {
		return new Jamba();
	}
	
	public boolean mugatu() {
		return true;
	}
	
	
}

class Mokie extends Jamba {
	boolean a;
	boolean b;
	int f2;
	int arr;

	
	public boolean broko(int a) {
		return true;
	}	
	
	public int mufu() {
		return 500;
	}
	
	public boolean mufasa() {
		f2 = 11111;
		return false;
	}
	
	public int roko() {
		Mokie m;
		return this.mufu();
	}
	
	public int mojoJojo(int a) {
		return f2;
	}
}

class Tootsie extends Mokie {
	Jamba f2;
	int b;
	int []arr;
	
	public int jookie(int a, boolean mojo) {
		return a;
	}
	
	public int mufu() {
		return b;
	}
	
	public int yolo(int a, boolean b) {
		return 400;
	}
	
}

class Snookie extends Tootsie {
	int f2;
	public int snookieTest() {
		int temp;
		boolean bo;
		int f2;
		
		bo = this.mufasa();
		temp = this.mokuMoku();
		f2 = 12345;
		System.out.println(f2);
		System.out.println(this.mojoJojo(f2));
		System.out.println(this.mokuMoku());
		b = 23;
		System.out.println(this.mufu());
		
		arr = new int[11];
		arr[2] = 545454;
		System.out.println(arr[2]);
		
		return f2;
	}
	
	public int mokuMoku() {
		f2 = 989898;
		return f2;
	}
}

