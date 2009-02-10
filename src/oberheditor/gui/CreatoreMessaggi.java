package oberheditor.gui;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.SysexMessage;

import oberheditor.Scaletta;
import oberheditor.SysexReceiver;

public class CreatoreMessaggi {
	private Scaletta scaletta;
	private byte[] HEAD_MSG; 
	
	
	public CreatoreMessaggi(Scaletta scaletta) {
		// TODO Auto-generated constructor stub
		this.scaletta = scaletta;
		
		HEAD_MSG = new byte[] {(byte) 0xF0, (byte) 0x7E, (byte) 0x7F, (byte) 0x00, (byte) 0x02, (byte) 0x01};
	}

	public Vector<SysexMessage> creaMessaggi() {
		int contatore;
		if (scaletta == null) {
			System.out.println("Errore: scaletta non caricata.");
			return null;
		}
		Vector<SysexMessage> result = new Vector<SysexMessage>();
		
		/********** TEEEEEEEEMPPPP ************/
		int id_chain = 0;
		String nomeChain = this.scaletta.getNome();
		nomeChain = String.format("%1$-" + 12 + "s", nomeChain); // Pad right
		nomeChain = nomeChain.substring(0,12);
		
		// pad left: String.format("%1$#" + n + "s", s);
		/********** TEEEEEEEEMPPPP ************/
		
		
		/****** HEADER *******/
		boolean finito = false;
		
		
		contatore = 12 * id_chain;
		
		while (!finito) {
			int puntatore = 0;
			byte[] bytes = new byte[75];
			for (int i = 0; i < 6; i++) {
				bytes[i] = HEAD_MSG[i];
			}
			
			// Calcolo i 4 bytes ADDR
			byte[] addr = calcolaAddrBytes(contatore, 0x7A, 0x75);
			for (int i = 0; i < addr.length; i++) {
				bytes[6 + i] = addr[i];
			}
			
			puntatore = 10; // 6 head + 4 addr
			// Nome scaletta
			char[] nomi = nomeChain.toCharArray();
			for (int i = 0; i < nomi.length; i++) {
				if (i % 7 == 0) {
					// metto la maschera, che qui è sempre 0x00
					bytes[puntatore++] = (byte) 0x00;
				}
				bytes[puntatore++] = (byte) nomi[i];
			}
			
			// Byte di chiusura
			bytes[puntatore] = (byte) 0xF7;
			
			//System.out.println(SysexReceiver.getHexString(bytes));
			
			SysexMessage sysex = new SysexMessage();
			try {
				sysex.setMessage(bytes, puntatore + 1);
				System.out.println(SysexReceiver.getHexString(sysex.getMessage()));
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			result.add(sysex);
			
			finito = true; // TODO non è vero, è solo il caso con una chain
		}
		
		/****** DATA *******/
		finito = false;
		contatore = 512 * id_chain;
		
		// Prendo i dati veri e propri
		byte[] dati = scaletta.toByteArray();
		int puntatoreDati = 0;
		
		while (!finito) {
			int puntatore = 0;
			byte[] bytes = new byte[75];
			for (int i = 0; i < 6; i++) {
				bytes[i] = HEAD_MSG[i];
			}
			
			// Calcolo i 4 bytes ADDR
			byte[] addr = calcolaAddrBytes(contatore, 0x00, 0x70);
			for (int i = 0; i < addr.length; i++) {
				bytes[6 + i] = addr[i];
			}
			
			puntatore = 10; // 6 head + 4 addr
			// Dati veri e propri
			for (int i = 0; i < dati.length; i++) {
				if (i % 7 == 0) {
					// metto la maschera
					byte mask = 0x00;
					for (int j = 1; j < 8; j++) {
						int n = 0;
						if ((puntatoreDati + j - 1) < dati.length && dati[puntatoreDati + j - 1] == (byte) 0xFF)
						 n = 0x01 << 8 - j - 1;
						mask |= n;
					}
					
					bytes[puntatore++] = (byte) mask; // TODO: mettere la maschera vera
				}
				if (dati[puntatoreDati] == (byte) 0xFF) {
					// Ho finito i dati reali, riempio con 0x7F
					bytes[puntatore++] = (byte) 0x7F;
					puntatoreDati++;
				}
				else {
					bytes[puntatore++] = (byte) dati[puntatoreDati++];
				}
				
				if (puntatore >= bytes.length - 1) { // -1 perche` l'ultimo e' il byte di chiusura
					// Continuo al prossimo giro
					break;
				}
				if (puntatoreDati >= dati.length) {
					finito = true;
					break; // finito
				}
			}
			
			// Byte di chiusura
			bytes[puntatore] = (byte) 0xF7;
			
			contatore += 0x38;
			
			//System.out.println(SysexReceiver.getHexString(bytes));
			
			SysexMessage sysex = new SysexMessage();
			try {
				sysex.setMessage(bytes, puntatore + 1);
				System.out.println(SysexReceiver.getHexString(sysex.getMessage()));
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			result.add(sysex);
		}
		
		
		
		/****** FOOTER *******/
		finito = false;
		contatore = 2 * id_chain;
		
		dati = new byte[2]; // fisso, sarà 2 * quantità chains
		for (int i = 0; i < dati.length;i++) {
			// Controllo per il cambio: 0x02 = pedale 2
			dati[i] = (byte) (i % 2 == 0 ? 0x02 : 0x00);
		}
		
		puntatoreDati = 0;
		while (!finito) {
			int puntatore = 0;
			byte[] bytes = new byte[75];
			for (int i = 0; i < 6; i++) {
				bytes[i] = HEAD_MSG[i];
			}
			
			// Calcolo i 4 bytes ADDR
			byte[] addr = calcolaAddrBytes(contatore, 0x79, 0x75);
			for (int i = 0; i < addr.length; i++) {
				bytes[6 + i] = addr[i];
			}
			
			puntatore = 10; // 6 head + 4 addr
			// Dati veri e propri
			for (int i = 0; i < dati.length; i++) {
				if (i % 7 == 0) {
					// metto la maschera
					bytes[puntatore++] = (byte) 0x00; // la maschera è sempre 0x00 nel footer
				}
				bytes[puntatore++] = (byte) dati[puntatoreDati++];
				
				if (puntatore >= bytes.length - 1) { // -1 perche` l'ultimo e' il byte di chiusura
					// Continuo al prossimo giro
					break;
				}
				if (puntatoreDati >= dati.length) {
					finito = true;
					break; // finito
				}
			}
			
			// Byte di chiusura
			bytes[puntatore] = (byte) 0xF7;
			
			contatore += 0x38;
			
			//System.out.println(SysexReceiver.getHexString(bytes));
			
			SysexMessage sysex = new SysexMessage();
			try {
				sysex.setMessage(bytes, puntatore + 1);
				System.out.println(SysexReceiver.getHexString(sysex.getMessage()));
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			result.add(sysex);
			finito = true; // TODO: non è vero, temporaneo
		}
		
		salvaSyx(result);
		return result;
	}
	
	private void salvaSyx(Vector<SysexMessage> messaggi) {
		// TODO Auto-generated method stub
	// Salvataggio
		FileOutputStream fos; 
    DataOutputStream dos;

    try {
      File file= new File("scaletta.syx");
      fos = new FileOutputStream(file);
      dos=new DataOutputStream(fos);

      // Salvo tutti i messaggi
			for (SysexMessage msg : messaggi) {
				dos.write(msg.getMessage());
			}
			dos.close();
			
    } catch (IOException e) {
      e.printStackTrace();
    }

	}

	private byte[] calcolaAddrBytes(int contatore, int start2, int start4) {
		byte[] result = new byte[4];
		int a = (contatore & ~0x7FFF) >> 15;
		int b = (contatore & 0x7F00) >> 8;
		int c = (contatore & 0x80) >> 7;
		int d = contatore & 0x7F;
		
		// ADDR1
		result[0] = (byte) (c== 0 ? 0x50 : 0x70);
		// ADDR2
		result[1] = (byte) (start2 + b);
		// ADDR3
		result[2] = (byte) d;
		// ADDR4
		result[3] = (byte) (start4 + a);
		return result;
	}

	public void setScaletta(Scaletta scaletta) {
		this.scaletta = scaletta;
	}

	public Scaletta getScaletta() {
		return scaletta;
	}

}