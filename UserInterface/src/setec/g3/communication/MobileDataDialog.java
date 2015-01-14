package setec.g3.communication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class MobileDataDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Por favor, active os dados móveis. Carregue OK para ir às definições.")
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        //open settings (mobile data)
                	    Intent intent = new Intent();
                   		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   		intent.setAction(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                   		startActivity(intent);
                   }
               })
               .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       //Noting?
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}