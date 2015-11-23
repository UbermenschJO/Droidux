package info.izumin.android.droidux.action;

import info.izumin.android.droidux.Store;

/**
 * Created by izumin on 11/24/15.
 */
public class RedoAction extends HistoryAction {
    public static final String TAG = RedoAction.class.getSimpleName();

    public RedoAction(Class<? extends Store> target) {
        super(Kind.REDO, target);
    }
}
