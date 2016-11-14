package Ejercicio2;

import java.util.concurrent.Semaphore;
import java.util.PriorityQueue;

class ITV {
	private Semaphore semaforo;
	private PriorityQueue <Integer> listaCoches;
	private Integer tiempoTotal;

	public ITV() {
		semaforo = new Semaphore(1);
		listaCoches = new PriorityQueue <Integer>();
		tiempoTotal = 0;
	}

	public void nuevoCoche(Integer numeroCoche) {
		try {
			semaforo.acquire();
			listaCoches.add(numeroCoche);
			semaforo.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int terminarCoche(Integer tiempoParcial) {
		int coche=0;
		try {
			if (isCochesPendientes()) {
				semaforo.acquire();
				coche = listaCoches.poll();
				tiempoTotal += tiempoParcial;
				semaforo.release();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return coche;
	}

	public boolean isCochesPendientes() {
		return listaCoches.size() > 0;
	}
	
	public Integer getTiempoTotal () {
		return tiempoTotal;
	}
	
}

class Puesto extends Thread {
	private int identif;
	private ITV itv;
	private Integer tiempoPuesto;

	public Puesto(int identif, ITV itv) {
		this.identif = identif;
		this.itv = itv;
		this.tiempoPuesto=0;
	}

	public void run() {
		int retardo;
		int numeroCoche;
		while (itv.isCochesPendientes()) {
			try {
				retardo = (int) (Math.random() * 90 + 10);
				tiempoPuesto +=retardo;
				numeroCoche=itv.terminarCoche(retardo);
				sleep(retardo);
				System.out.println("El puesto " + identif + " ha revisado el coche " + numeroCoche + " en un tiempo de " + retardo);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Fin del puesto " + identif + ", que termina con un tiempo parcial de " + tiempoPuesto);
	}
}

class Vehiculo extends Thread {
	private int identif;
	private ITV itv;

	public Vehiculo(int identif, ITV itv) {
		this.identif = identif;
		this.itv = itv;
	}

	public void run() {
		itv.nuevoCoche(identif);
	}
}

public class Principal {

	public static void main(String[] args) {
		int pueRandom = (int) (Math.random() * 4) + 1;
		int vehRandom = (int) (Math.random() * 30) + 20;
		ITV itv = new ITV();
		System.out.println(vehRandom + " Vehículos serán atendidos por " + pueRandom + " puestos.");
		// Creación de vehículos
		Vehiculo[] v = new Vehiculo[vehRandom];
		for (int i = 0; i < vehRandom; i++) {
			v[i] = new Vehiculo(i + 1, itv);
			v[i].start();
		}
		// Creación de puestos
		Puesto[] p = new Puesto[pueRandom];
		for (int i = 0; i < pueRandom; i++) {
			p[i] = new Puesto(i + 1, itv);
			p[i].start();
		}

		// Se espera a que terminen todos los puestos
		for (int i = 0; i < pueRandom; i++) {
			try {
				p[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Se espera a que terminen todos vehículos
		for (int i = 0; i < vehRandom; i++) {
			try {
				v[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Se cierra la itv
		System.out.println("Se cierra la ITV con un tiempo acumulado de " + itv.getTiempoTotal());
	}
}
