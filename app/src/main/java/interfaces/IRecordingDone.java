package interfaces;

public interface IRecordingDone {
    //ris -1 se non ha indovinato niente altrimenti
    //0 - ha indovinato la prima
    //1 ha indovinato la seconda
    //2 ha indovinato la terza
    void onRecordingDone(int result, String resultSt);
}
