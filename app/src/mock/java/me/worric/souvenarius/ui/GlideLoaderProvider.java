package me.worric.souvenarius.ui;

public class GlideLoaderProvider {

    private static GlideLoader INSTANCE;

    public static GlideLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (GlideLoader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MockGlideLoaderImpl();
                }
            }
        }
        return INSTANCE;
    }

}
