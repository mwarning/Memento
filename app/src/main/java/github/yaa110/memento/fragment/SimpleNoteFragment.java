package github.yaa110.memento.fragment;

import android.view.View;

import androidx.core.content.ContextCompat;

import github.yaa110.memento.R;
import github.yaa110.memento.fragment.template.NoteFragment;
import github.yaa110.memento.model.DatabaseModel;
import jp.wasabeef.richeditor.RichEditor;

public class SimpleNoteFragment extends NoteFragment {
    private RichEditor body;

    public SimpleNoteFragment() {
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_simple_note;
    }

    @Override
    public void saveNote(final SaveListener listener) {
        super.saveNote(listener);
        note.body = body.getHtml();

        new Thread() {
            @Override
            public void run() {
                long id = note.save();
                if (note.id == DatabaseModel.NEW_MODEL_ID) {
                    note.id = id;
                }
                listener.onSave();
                interrupt();
            }
        }.start();
    }

    @Override
    public void init(View view) {
        body = view.findViewById(R.id.editor);
        body.setPlaceholder("Note");
        body.setEditorBackgroundColor(ContextCompat.getColor(getContext(), R.color.bg));

        view.findViewById(R.id.action_bold).setOnClickListener(v -> body.setBold());

        view.findViewById(R.id.action_italic).setOnClickListener(v -> body.setItalic());

        view.findViewById(R.id.action_underline).setOnClickListener(v -> body.setUnderline());

        body.setHtml(note.body);
    }
}
