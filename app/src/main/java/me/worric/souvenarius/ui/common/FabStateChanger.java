package me.worric.souvenarius.ui.common;

import me.worric.souvenarius.ui.main.FabState;

/* TODO change this to instead rely on FragmentManagerCallback#onFragmentResumed in the host activity */
public interface FabStateChanger {
    void changeFabState(FabState state);
}
