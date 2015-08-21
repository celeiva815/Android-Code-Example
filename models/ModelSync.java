package social.laika.app.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

import social.laika.app.utils.Tag;

/**
 * Created by Tito_Leiva on 17-08-15.
 */
public class ModelSync extends Model {

    public final static String COLUMN_NEEDS_SYNC = "needs_sync";

    @Column(name = COLUMN_NEEDS_SYNC)
    public int mNeedsSync;


    public void create() {

        this.mNeedsSync = Tag.FLAG_CREATED;
        this.save();
        Log.i("Laika Sync Service", this.getClass().getSimpleName() + " created. Local ID:" + getId() + ". Need Sync");

    }

    public void refresh() {

        this.mNeedsSync = Tag.FLAG_READED;
        this.save();

        Log.i("Laika Sync Service", this.getClass().getSimpleName() + " refreshed. Local ID: " + getId() + ". " +
                "Server Id: " + getServerId() + ".");
    }

    public void update() {

        if (this.mNeedsSync == Tag.FLAG_READED) {
            this.mNeedsSync = Tag.FLAG_UPDATED;
        }
        this.save();

        Log.i("Laika Sync Service", this.getClass().getSimpleName() + " updated. Local ID: " + getId() + ". " +
                "Server Id: " + getServerId() + ". Need Sync");
    }

    public void remove() {

        if (mNeedsSync == Tag.FLAG_CREATED) {

            Log.i("Laika Sync Service", this.getClass().getSimpleName() +" deleted. Local ID: " + getId());
            this.delete();
        } else {

            this.mNeedsSync = Tag.FLAG_DELETED;
            this.save();

            Log.i("Laika Sync Service", this.getClass().getSimpleName() + " removed. Local ID: " + getId() + ". " +
                    "Server Id: " + getServerId() + ". Need Sync");
        }


    }

    public int getServerId() {

        return -2;
    }

}
