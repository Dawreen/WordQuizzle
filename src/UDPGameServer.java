import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Classe che esegue il lato server della sfida.
 */
public class UDPGameServer extends UDPServer{
    public static final int T2 = 30_000; // tempo massimo di una partita

    private final String[] words;

    private final String p1ID;
    private final Session player1;
    private final String p2ID;
    private final Session player2;

    private final String[] tradP1;
    private final String[] tradP2;

    private boolean p1Finito;
    private boolean p2Finito;

    private int timeP1;
    private int timeP2;

    /**
     * Costruttore.
     * @param player1 istanza della session del giocatore 1 (sfidante)
     * @param player2 istanza della session del giocatore 2 (sfidato)
     * @param port porta dulla quale settare la connessione UDP
     * @param words parole che veranno inviate durante la sfida
     */
    public UDPGameServer(Session player1, Session player2, int port, String[] words) {
        super(port);
        this.words = words;

        this.tradP1 = new String[words.length];
        this.tradP2 = new String[words.length];

        this.player1 = player1;
        this.p1ID = player1.getUserID();
        this.p1Finito = false;

        this.player2 = player2;
        this.p2ID = player2.getUserID();
        this.p2Finito = false;
    }

    @Override
    public void respond(DatagramSocket socket, DatagramPacket packet) throws IOException {
        //noinspection CharsetObjectCanBeUsed
        String s = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
        String[] sSplit = s.split("_");
        boolean trdIsBlank = sSplit.length == 2;

        String currentPlayer = sSplit[0]; // da quale giocatore è arrivato il pacchetto
        int index = 0;
        try {
            index = Integer.parseInt(sSplit[1]); // indice della prossima parola che si vuole tradurre
        } catch (NumberFormatException ex){ // è stato inviato il tempo
            if (currentPlayer.equals(p1ID)) {
                this.timeP1 = Integer.parseInt(sSplit[2]);
                this.p1Finito = true;
            }
            if (currentPlayer.equals(p2ID)) {
                this.timeP2 = Integer.parseInt(sSplit[2]);
                this.p2Finito = true;
            }
        }
        if (index == 0) { // al primo messaggio di ogni giocatore viene fatto partire il timer
            Timer timer = new Timer();
            timer.schedule(new GameTimer(socket, packet.getAddress(), packet.getPort()), T2, 2_000);
        }
        String trad = null;
        if (!trdIsBlank) trad = sSplit[2]; // traduzione della parola precedentemente inviata

        if (index > 0) { // nel primo messaggio dal client non vi è una traduzione utile
            if (currentPlayer.equals(p1ID) && !this.p1Finito) {
                tradP1[index-1] = trad;
            }
            if (currentPlayer.equals(p2ID) && !this.p2Finito) {
                tradP2[index-1] = trad;
            }
        }

        if (this.p1Finito && this.p2Finito) {
            punteggio(); // calcolo dei punteggi
            return;
        }

        if (index < words.length) { // invio della parola successiva da tradurre
            byte[] data = words[index].getBytes(StandardCharsets.UTF_8);
            DatagramPacket out = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
            socket.send(out);
        } else { // invio messaggio di fine della sfida
            byte[] data = "FINE".getBytes();
            DatagramPacket out = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
            socket.send(out);
        }
    }

    public static int X = 2; // in caso di risposta corretta
    public static int Y = 1; // in caso di risposta errata

    /**
     * Calcola il punteggio a fine partita.
     */
    private void punteggio() {
        int scoreP1 = 0;
        int scoreP2 = 0;
        try {
            for (int i = 0; i < words.length; i++) {
                //System.out.println("da tradurre: " + words[i]);
                //System.out.println("traduzione p1: " + tradP1[i]);
                //System.out.println("traduzione p2: " + tradP2[i]);
                ArrayList<String> translations = getTranslation(words[i]); // possibili traduzioni
                //System.out.println("possibili traduzioni:\n" + translations.toString());
                if (tradP1[i] != null) { // traduzione giocatore1
                    if (translations.contains(tradP1[i])) {
                        scoreP1 = scoreP1 + X;
                    } else {
                        scoreP1 = scoreP1 - Y;
                    }
                }
                if (tradP2[i] != null) { // traduzione giocatore 2
                    if (translations.contains(tradP2[i])) {
                        scoreP2 = scoreP2 + X;
                    } else {
                        scoreP2 = scoreP2 - Y;
                    }
                }
            }
            // comunica vincitore e perdente
            if (scoreP1 > scoreP2) {
                this.player1.vincitore();
                this.player2.perdente();
            } else if (scoreP2 > scoreP1) {
                this.player1.perdente();
                this.player2.vincitore();
            } else { // quando si è indovinate lo stesso numero di parole si considera il tempo
                if (this.timeP1 < this.timeP2) {
                    this.player1.vincitore();
                    this.player2.perdente();
                } else if (this.timeP1 > this.timeP2) {
                    this.player1.perdente();
                    this.player2.vincitore();
                } else {
                    this.player1.pareggio();
                    this.player2.pareggio();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere la traduzione di un parola tramite una chiamata HTTP get
     * @param word parola che si vuole tradurre
     * @return restituisce un'arraylist contente le possibili traduzioni
     * @throws IOException se la http get fa errore
     */
    private ArrayList<String> getTranslation(String word) throws IOException {
        ArrayList<String> translation = new ArrayList<>();

        // Settando la chiamata GET.
        // E' possibile tradurre parole composite quindi gli spazi devono essere sostituiti.
        String HTTPrequest =
                "https://api.mymemory.translated.net/get?q=" +
                        word.replace(" ", "%20") + "&langpair=it|en";

        // Creando un la classe per parsare il risultato della GET.
        JsonFactory jFactory = new JsonFactory();
        JsonParser jParser = jFactory.createParser(new URL(HTTPrequest));

        // mappando l'oggetto ad un json node
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jn = mapper.readTree(jParser);

        final JsonNode jnGet = jn.get("matches");
        // Looping until the "}" toke is found.
        for (final JsonNode each : jnGet) {
            JsonNode trad = each.get("translation"); // estraendo ciò che interessa

            // Rimozione di numeri e caratteri speciali. Settando le string in lowercase,
            // per evitare errori non esistenti.
            translation.add(trad.toString().toLowerCase().replaceAll("[^a-zA-Z0\\u0020]", "")
                    .replaceAll("[0123456789]", ""));
        }

        return translation;
    }
}
