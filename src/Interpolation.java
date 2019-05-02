import java.io.*;

public class Interpolation {

	public static void main(String[] args) throws IOException {
		Axon.inertie = 0.3f;
		for(int n = 1; n <= 10; n++) {
			for(int p = 1; p <= 30; p++) {
				float errt = 0;
				for(int j = 0; j < 20; j++) {
					float err = 0;
					Network network = new Network();
					network.createNetwork(new int[]{2, n, 1}, 2, 1, 0.3f);
					float[][] in = new float[p][2];
					float[][] out = new float[p][1];
					for(int i = 0; i < p; i++) {
						in[i] = new float[]{(float) Math.random()*10, (float) Math.random()*10};
						out[i] = new float[]{(int) (Math.random()*2)};
					}
					network.connectAll(0, 1, 2);
					network.connectAll(1, 2, 2);
					network.addSeuil(new int[]{1, 2}, 2);
					for(int i = 0; i < 200000; i++) {
		 				int r = i%p;
						float res = network.learn(in[r], out[r])[0];
						if(i >= 200000-p) err += (out[r][0] - res) * (out[r][0] - res) / p;
					}
					if(err < 0.005f) errt ++;
				}
				System.out.print(errt + " ");
			}
			System.out.println();
		}
	}

}