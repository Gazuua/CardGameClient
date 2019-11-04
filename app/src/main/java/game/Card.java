package game;

public class Card {
    // 망통~한끗
    public static final int CARD_MANGTONG = 90;
    public static final int CARD_HANKUT = 91;
    public static final int CARD_DUKUT = 92;
    public static final int CARD_SEKUT = 93;
    public static final int CARD_NEKUT = 94;
    public static final int CARD_DASUTKUT = 95;
    public static final int CARD_YUSUTKUT = 96;
    public static final int CARD_ILGOPKUT = 97;
    public static final int CARD_YUDULKUT = 98;

    // 갑오~알리 및 사구
    public static final int CARD_GABO = 100;
    public static final int CARD_SERYUK = 101;
    public static final int CARD_JANGSA = 102;
    public static final int CARD_JANGBBING = 103;
    public static final int CARD_GUBBING = 104;
    public static final int CARD_ALLI = 105;
    public static final int CARD_SAGU_NORMAL = 150;

    // 삥땡~구땡
    public static final int CARD_BBINGTTAENG = 201;
    public static final int CARD_YITTAENG = 202;
    public static final int CARE_SAMTTAENG = 203;
    public static final int CARD_SATTAENG = 204;
    public static final int CARD_OTTAENG = 205;
    public static final int CARD_YUKTTAENG = 206;
    public static final int CARD_CHILTTAENG = 207;
    public static final int CARD_PPALTTAENG = 208;
    public static final int CARD_GUTTAENG = 209;

// 땡잡이, 멍사구, 장땡
    public static final int CARD_TTAENGJABI = 210;
    public static final int CARD_SAGU_MUNGTUNGGURI = 250;
    public static final int CARD_JANGTTAENG = 299;

// 일삼, 일팔광땡 및 암행어사
    public static final int CARD_ILSAMG_GWANGTTAENG = 301;
    public static final int CARD_ILPAL_GWANGTTAENG = 302;
    public static final int CARD_INSPECTOR = 310;

// 무적의 삼팔광땡
    public static final int CARD_SAMPAL_GWANGTTAENG = 99999;

    private int cardNumber;
    private boolean bSpecial;

    public Card(){
        cardNumber = -1;
        bSpecial = false;
    }

    public Card(int num, boolean special) {
        this.cardNumber = num;
        this.bSpecial = special;
    }

    public int getNumber() { return this.cardNumber; }
    public boolean getSpecial() { return this.bSpecial; }
}
