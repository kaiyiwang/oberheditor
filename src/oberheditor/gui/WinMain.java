package oberheditor.gui;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import oberheditor.Canzone;
import oberheditor.Database;
import oberheditor.Scaletta;
import oberheditor.midi.CreatoreMessaggi;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;


public class WinMain {
	private Shell win;
	private Vector<Scaletta> scalette;
	private Table listScalette;
	private Button btnNuovaScaletta;
	private Button btnModificaScaletta;
	private Button btnInviaScaletta;
	private Button btnEliminaScaletta;
	private Display display;
	private MenuItem mnuEsportaSyx;
	private Vector<Canzone> canzoni;
	private List listCanzoni;
	private Button btnNuovaCanzone;
	private Button btnModificaCanzone;
	private Button btnEliminaCanzone;
	
	public WinMain(Display _display) {
		this.display = _display;
		win = new Shell(display);
		win.setText("OberhEditor");
		
		int win_w = 780;
		int win_h = 500;
		// La metto centrata
		int pos_x = (display.getBounds().width - win_w) / 2;
		int pos_y = (display.getBounds().height - win_h) / 2;
		win.setBounds(pos_x, pos_y, win_w, win_h);
		
		FormLayout layout = new FormLayout();
		win.setLayout(layout);
		
		Menu bar = new Menu (win, SWT.BAR);
		win.setMenuBar (bar);
		MenuItem fileItem = new MenuItem (bar, SWT.CASCADE);
		fileItem.setText ("&File");
		Menu submenu = new Menu (win, SWT.DROP_DOWN);
		fileItem.setMenu (submenu);
		mnuEsportaSyx = new MenuItem (submenu, SWT.PUSH);
		mnuEsportaSyx.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				// se non ho alcuna scaletta selezionata, esco
				if (listScalette.getSelectionCount() <= 0) return;
				// TODO: implementare il salvataggio di piu' scalette insieme
				
				// Mostro la finestra di dialogo per il salvataggio
				FileDialog dialog = new FileDialog (win, SWT.SAVE);
				dialog.setFilterNames (new String [] {"File SysEx", "Tutti i files(*.*)"});
				dialog.setFilterExtensions (new String [] {"*.syx", "*.*"}); //Windows wild cards
				dialog.setFileName(listScalette.getSelection()[0].getText(0).toLowerCase() + ".syx");
				String path = dialog.open();
				if (path == null) return;
				CreatoreMessaggi cm = new CreatoreMessaggi(scalette.get(listScalette.getSelectionIndex()));
				// TODO: far scegliere in quale posizione la voglio salvare
				cm.salvaSyx(cm.creaMessaggi(0), path);
				// TODO: mostrare errore se c'e' un problema nel salvataggio
			}
		});
		mnuEsportaSyx.setText("Esporta come file .syx\tCtrl+S");
		mnuEsportaSyx.setAccelerator (SWT.MOD1 + 'S');

		
		Label lblScalette = new Label(win, SWT.NONE);
		lblScalette.setText("Scalette:");
		FormData layLblScalette = new FormData();
		layLblScalette.left = new FormAttachment(0, 10);
		layLblScalette.top = new FormAttachment(0, 10);
		lblScalette.setLayoutData(layLblScalette);
		
		
		
		
		listScalette = new Table (win, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		listScalette.setLinesVisible (true);
		listScalette.setHeaderVisible (true);
		// Imposto le intestazioni
		String[] titles = {"Nome", "Data"};
		for (int i=0; i<titles.length; i++) {
			TableColumn column = new TableColumn (listScalette, SWT.NONE);
			column.setText (titles [i]);
		}
		
		listScalette.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				refreshTastiScaletta();
			}
		});
		
		listScalette.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				modificaScalette();
			}
		});
		
		
		FormData layListScalette = new FormData();
		layListScalette.left = new FormAttachment(lblScalette, 0, SWT.LEFT);
		layListScalette.top = new FormAttachment(lblScalette, 10, SWT.BOTTOM);
		layListScalette.width = 180;
		layListScalette.bottom = new FormAttachment(100, -30);
		listScalette.setLayoutData(layListScalette);
		// Imposto la dimensione delle colonne
		listScalette.getColumn(1).pack();
		listScalette.getColumn(0).setWidth(110);

		/******************************************
		 *      PULSANTI SCALETTE
		 ******************************************/
		btnNuovaScaletta = new Button(win, SWT.PUSH);
		btnNuovaScaletta.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new WinScaletta(win);
				// TODO: solo se ho confermato il salva
				caricaScalette();
			}
		});
		Image imgNuovo = new Image(display, "res/add.png");
		btnNuovaScaletta.setImage(imgNuovo);
		btnNuovaScaletta.setText("Nuova Scaletta");
		FormData layBtnNuovaScaletta = new FormData();
		layBtnNuovaScaletta.left = new FormAttachment(listScalette, 10, SWT.RIGHT);
		layBtnNuovaScaletta.top = new FormAttachment(listScalette, 20, SWT.TOP);
		layBtnNuovaScaletta.width = 150;
		btnNuovaScaletta.setLayoutData(layBtnNuovaScaletta);
		
		btnModificaScaletta = new Button(win, SWT.PUSH);
		btnModificaScaletta.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				modificaScalette();
			}
		});
		Image imgModifica = new Image(display, "res/edit.png");
		btnModificaScaletta.setImage(imgModifica);
		btnModificaScaletta.setText("Modifica Scaletta");
		FormData layBtnModificaScaletta = new FormData();
		layBtnModificaScaletta.left = new FormAttachment(btnNuovaScaletta, 0, SWT.LEFT);
		layBtnModificaScaletta.top = new FormAttachment(btnNuovaScaletta, 10, SWT.BOTTOM);
		layBtnModificaScaletta.width = 150;
		btnModificaScaletta.setLayoutData(layBtnModificaScaletta);
		
		btnInviaScaletta = new Button(win, SWT.PUSH);
		btnInviaScaletta.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listScalette.getSelectionCount() <= 0) return;
				new WinInviaSysex(win, scalette.get(listScalette.getSelectionIndex()));
			}
		});
		Image imgInvia = new Image(display, "res/send.png");
		btnInviaScaletta.setImage(imgInvia);
		btnInviaScaletta.setText("Invia alla tastiera");
		FormData layBtnInviaScaletta = new FormData();
		layBtnInviaScaletta.left = new FormAttachment(btnNuovaScaletta, 0, SWT.LEFT);
		layBtnInviaScaletta.top = new FormAttachment(btnModificaScaletta, 10, SWT.BOTTOM);
		layBtnInviaScaletta.width = 150;
		btnInviaScaletta.setLayoutData(layBtnInviaScaletta);
		
		
		btnEliminaScaletta = new Button(win, SWT.PUSH);
		btnEliminaScaletta.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listScalette.getSelectionCount() <= 0) return;
				// Chiedo conferma con un dialogo
				MessageBox boxChiedi = new MessageBox(win, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				boxChiedi.setText("Conferma");
				boxChiedi.setMessage("Vuoi davvero eliminare la scaletta selezionata?");
				
				if (boxChiedi.open() != SWT.OK) return;
				
				int[] selezione = listScalette.getSelectionIndices();
				for (int i = selezione.length -1; i >= 0 ; i--) {
					// dalla lista
					listScalette.remove(selezione[i]);
					// dal db
					// TODO: magari unificare con una query unica, invece che mille query nel for
					int id = scalette.get(selezione[i]).getId();
					try {
						Database.queryUp("DELETE FROM scaletta WHERE id = ?", id+"");
						Database.queryUp("DELETE FROM scaletta_canzone WHERE id_scaletta = ?", id+"");
						// dal vettore delle scalette
						scalette.remove(selezione[i]);
						refreshTastiScaletta();
					} catch (SQLException e1) {
						Main.errorBox(win, "Errore nell'eliminazione.\n" + e1.getMessage());
						e1.printStackTrace();
					}
				}
			}
		});
		Image imgElimina = new Image(display, "res/delete.png");
		btnEliminaScaletta.setImage(imgElimina);
		btnEliminaScaletta.setText("Elimina Scaletta");
		FormData layBtnEliminaScaletta = new FormData();
		layBtnEliminaScaletta.left = new FormAttachment(btnNuovaScaletta, 0, SWT.LEFT);
		layBtnEliminaScaletta.top = new FormAttachment(btnInviaScaletta, 10, SWT.BOTTOM);
		layBtnEliminaScaletta.width = 150;
		btnEliminaScaletta.setLayoutData(layBtnEliminaScaletta);
		
		caricaScalette();
		
		
		
		/**********************************************************
		 *                  CANZONI
		 **********************************************************/
		
		
		listCanzoni = new List(win, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		FormData layListCanzoni = new FormData();
		layListCanzoni.left = new FormAttachment(50, 0);
		layListCanzoni.top = new FormAttachment(listScalette, 0, SWT.TOP);
		layListCanzoni.width = 180;
		layListCanzoni.bottom = new FormAttachment(listScalette, 0, SWT.BOTTOM);
		listCanzoni.setLayoutData(layListCanzoni);
		
		listCanzoni.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				modificaCanzoni();
			}
		});
		
		listCanzoni.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				// Su cambio di selezione, aggiorno lo stato abilitato dei pulsanti 
				refreshTastiCanzone();
			}
		});
		
		Label lblCanzoni = new Label(win, SWT.NONE);
		lblCanzoni.setText("Canzoni:");
		FormData layLblCanzoni = new FormData();
		layLblCanzoni.left = new FormAttachment(listCanzoni, 0, SWT.LEFT);
		layLblCanzoni.top = new FormAttachment(lblScalette, 0, SWT.TOP);
		lblCanzoni.setLayoutData(layLblCanzoni);
		
		
		
		btnNuovaCanzone = new Button(win, SWT.PUSH);
		btnNuovaCanzone.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new WinCanzone(win);
				// TODO: solo se ho confermato il salva
				caricaCanzoni();
			}
		});
		btnNuovaCanzone.setImage(imgNuovo);
		btnNuovaCanzone.setText("Nuova Canzone");
		FormData layBtnNuovaCanzone = new FormData();
		layBtnNuovaCanzone.left = new FormAttachment(listCanzoni, 10, SWT.RIGHT);
		layBtnNuovaCanzone.top = new FormAttachment(listCanzoni, 20, SWT.TOP);
		layBtnNuovaCanzone.width = 150;
		btnNuovaCanzone.setLayoutData(layBtnNuovaCanzone);
		
		btnModificaCanzone = new Button(win, SWT.PUSH);
		btnModificaCanzone.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				modificaCanzoni();
			}
		});
		btnModificaCanzone.setImage(imgModifica);
		btnModificaCanzone.setText("Modifica Canzone");
		FormData layBtnModificaCanzone = new FormData();
		layBtnModificaCanzone.left = new FormAttachment(btnNuovaCanzone, 0, SWT.LEFT);
		layBtnModificaCanzone.top = new FormAttachment(btnNuovaCanzone, 10, SWT.BOTTOM);
		layBtnModificaCanzone.width = 150;
		btnModificaCanzone.setLayoutData(layBtnModificaCanzone);
		
		btnEliminaCanzone = new Button(win, SWT.PUSH);
		btnEliminaCanzone.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listCanzoni.getSelectionCount() <= 0) return;
				// Chiedo conferma con un dialogo
				MessageBox boxChiedi = new MessageBox(win, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				boxChiedi.setText("Conferma");
				boxChiedi.setMessage("Vuoi davvero eliminare la canzone selezionata?\nSara` eliminata da tutte le scalette.");
				if (boxChiedi.open() != SWT.OK) return;
				
				int[] selezione = listCanzoni.getSelectionIndices();
				for (int i = selezione.length -1; i >= 0 ; i--) {
					// dalla lista
					listCanzoni.remove(selezione[i]);
					// dal db
					// TODO: magari unificare con una query unica, invece che mille query nel for
					int id = canzoni.get(selezione[i]).getId();
					try {
						Database.queryUp("DELETE FROM canzone WHERE id = ?", id+"");
						Database.queryUp("DELETE FROM scaletta_canzone WHERE id_canzone = ?", id+"");
						// dal vettore delle scalette
						canzoni.remove(selezione[i]);
						refreshTastiCanzone();
					} catch (SQLException e1) {
						Main.errorBox(win, "Errore nell'eliminazione.\n" + e1.getMessage());
						e1.printStackTrace();
					}
				}
			}
		});
		btnEliminaCanzone.setImage(imgElimina);
		btnEliminaCanzone.setText("Elimina Canzone");
		FormData layBtnEliminaCanzone = new FormData();
		layBtnEliminaCanzone.left = new FormAttachment(btnNuovaCanzone, 0, SWT.LEFT);
		layBtnEliminaCanzone.top = new FormAttachment(btnModificaCanzone, 10, SWT.BOTTOM);
		layBtnEliminaCanzone.width = 150;
		btnEliminaCanzone.setLayoutData(layBtnEliminaCanzone);
		
		
		caricaCanzoni();
		
		win.open();
		while (!win.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
		display.dispose();
	}
	
	private void modificaScalette() {
		if (listScalette.getSelectionCount() <= 0) return;
		WinScaletta winSc = new WinScaletta(win, scalette.get(listScalette.getSelectionIndex()).getId());
		if (winSc.hoFattoModifiche)
			caricaScalette();
	}
	
	private void modificaCanzoni() {
		if (listCanzoni.getSelectionCount() <= 0) return;
		WinCanzone winSong = new WinCanzone(win, canzoni.get(listCanzoni.getSelectionIndex()).getId());
		if (winSong.hoFattoModifiche)
			caricaCanzoni();
	}

	private void refreshTastiScaletta() {
		btnModificaScaletta.setEnabled(listScalette.getSelectionCount() > 0);
		btnEliminaScaletta.setEnabled(listScalette.getSelectionCount() > 0);
		btnInviaScaletta.setEnabled(listScalette.getSelectionCount() > 0);
		mnuEsportaSyx.setEnabled(listScalette.getSelectionCount() > 0);
	}
	
	private void refreshTastiCanzone() {
		btnModificaCanzone.setEnabled(listCanzoni.getSelectionCount() > 0);
		btnEliminaCanzone.setEnabled(listCanzoni.getSelectionCount() > 0);
	}

	private void caricaScalette() {
		ResultSet res;
		try {
			Database.creaTable(Database.TBL_SCALETTA);
			res = Database.query("SELECT id FROM scaletta ORDER BY data DESC, id DESC;");
		} catch (SQLException e1) {
			Main.errorBox(win, "Errore nel caricamento.\n" + e1.getMessage());
			e1.printStackTrace();
			return;
		}
		
		// Se avevo gia' selezione, la ripristino dopo aver ricaricato.
		int [] oldSelectedIndexes = listScalette.getSelectionIndices();
		Vector<Integer> oldSelectedIds = new Vector<Integer>();
		if (oldSelectedIndexes.length > 0) {
			// Avevo una selezione, salvo gli id
			for (int i = 0; i < oldSelectedIndexes.length; i++) {
				oldSelectedIds.add(scalette.get(oldSelectedIndexes[i]).getId());
			}
		}
		
		scalette = new Vector<Scaletta>();
		listScalette.removeAll();
		
		try {
			Vector<Integer> id_scalette = new Vector<Integer>();
			while (res.next()) {
				// Le carico in memoria
				id_scalette.add(res.getInt("id"));
			}
			res.close();
			res.getStatement().close();
			for (Integer id : id_scalette) {
				Scaletta sc = new Scaletta(id);
				scalette.add(sc);
				TableItem item = new TableItem (listScalette, SWT.NONE);
				item.setText (0, sc.getNome());
				item.setText (1, new SimpleDateFormat("dd/MM/yyyy").format(sc.getData().getTime()));
				if (oldSelectedIds.contains(id))
					listScalette.select(listScalette.getItemCount() - 1);
			}
		} catch (SQLException e) {
			Main.errorBox(win, "Errore di connessione al database.");
			e.printStackTrace();
		}
		refreshTastiScaletta();
	}
	
	private void caricaCanzoni() {
		ResultSet res;
		try {
			Database.creaTable(Database.TBL_CANZONE);
			res = Database.query("SELECT id FROM canzone ORDER BY nome ASC;");
		} catch (SQLException e1) {
			Main.errorBox(win, "Errore nel caricamento.\n" + e1.getMessage());
			e1.printStackTrace();
			return;
		}
		
		
		// Se avevo gia' selezione, la ripristino dopo aver ricaricato.
		int [] oldSelectedIndexes = listCanzoni.getSelectionIndices();
		Vector<Integer> oldSelectedIds = new Vector<Integer>();
		if (oldSelectedIndexes.length > 0) {
			// Avevo una selezione, salvo gli id
			for (int i = 0; i < oldSelectedIndexes.length; i++) {
				oldSelectedIds.add(canzoni.get(oldSelectedIndexes[i]).getId());
			}
		}
		canzoni = new Vector<Canzone>();
		listCanzoni.removeAll();
		
		try {
			Vector<Integer> id_canzoni = new Vector<Integer>();
			while (res.next()) {
				// Le carico in memoria
				id_canzoni.add(res.getInt("id"));
			}
			res.close();
			res.getStatement().close();
			for (Integer id : id_canzoni) {
				Canzone song = new Canzone(id);
				canzoni.add(song);
				listCanzoni.add(song.getNome());
				if (oldSelectedIds.contains(id))
					listCanzoni.select(listCanzoni.getItemCount() - 1);
			}
		} catch (SQLException e) {
			Main.errorBox(win, "Errore di connessione al database.");
			e.printStackTrace();
		}
		refreshTastiCanzone();
	}
}
