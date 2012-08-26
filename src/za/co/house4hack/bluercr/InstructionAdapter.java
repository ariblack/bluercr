package za.co.house4hack.bluercr;

import java.text.DecimalFormat;
import java.util.List;

import za.co.house4hack.bluercr.Instruction.InstructionCommand;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class InstructionAdapter extends BaseAdapter {

	private List<Instruction> mInstructionList;
	private LayoutInflater mInflater;

	public InstructionAdapter(List<Instruction> list, LayoutInflater inflater) {
		mInstructionList = list;
		mInflater = inflater;
	}

	@Override
	public int getCount() {
		return mInstructionList.size();
	}

	@Override
	public Object getItem(int position) {
		return mInstructionList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewItem item;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.instruction, null);
			item = new ViewItem(this, convertView.getContext());

			item.idView = (TextView) convertView
					.findViewById(R.id.instructionid);
			item.commandImage = (ImageView) convertView
					.findViewById(R.id.commandicon);
			item.descriptionView = (TextView) convertView
					.findViewById(R.id.description);
			item.durationBar = (SeekBarEx) convertView
					.findViewById(R.id.durationbar);
			item.deleteButton = (ImageButton) convertView
					.findViewById(R.id.deleteinstruction);
			item.insertButton = (ImageButton) convertView
					.findViewById(R.id.insertinstruction);

			convertView.setTag(item);
		} else {
			item = (ViewItem) convertView.getTag();
		}

		item.instruction = mInstructionList.get(position);
		item.mPosition = position;
		item.setup();

		return convertView;
	}

	private class ViewItem implements OnClickListener, OnSeekBarChangeListener,
			OnLongClickListener {
		ImageButton insertButton;
		TextView idView;
		ImageView commandImage;
		TextView descriptionView;
		SeekBarEx durationBar;
		ImageButton deleteButton;
		Context mContext;
		InstructionAdapter parent;
		Instruction instruction;
		int mPosition;

		public ViewItem(InstructionAdapter instructionAdapter, Context context) {
			parent = instructionAdapter;
			mContext = context;
		}

		public void setup() {
			if (instruction != null) {
				idView.setText(Integer.toString(mPosition + 1));
				commandImage
						.setImageResource(instruction.lookupDrawable(instruction.command));
				commandImage.setOnLongClickListener(this);

				durationBar.setOnSeekBarChangeListener(this);
				durationBar.setProgress((int) (instruction.duration * 10));
				durationBar.setMax(100);

				deleteButton.setOnClickListener(this);
				insertButton.setOnClickListener(this);
				updateDescription();
			}

		}

		public void updateDescription() {
			DecimalFormat df = new DecimalFormat("#.#");
			descriptionView.setText(instruction.lookupCommandText(instruction.command)
					+ " for " + df.format(instruction.duration) + "s");

		}

		@Override
		public void onClick(View v) {
			if (v == deleteButton)
				doDelete();
			if (v == insertButton)
				doInsert();
		}

		private void doInsert() {
			parent.mInstructionList.add(mPosition + 1, instruction.clone());
			parent.notifyDataSetChanged();
		}

		private void doDelete() {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage(mContext.getString(R.string.deleteitem))
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									parent.mInstructionList.remove(mPosition);
									parent.notifyDataSetChanged();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			AlertDialog alert = builder.create();
			alert.show();

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			instruction.duration = progress / 10.0;
			updateDescription();

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// seekBar.setSecondaryProgress(seekBar.getProgress());

		}


		@Override
		public boolean onLongClick(View arg0) {
			final Dialog dialog = new Dialog(mContext);
			dialog.setContentView(R.layout.command_dialog);
			dialog.setTitle("Choose a command");

			ImageButton butLeft = (ImageButton) dialog
					.findViewById(R.id.butLeft);
			butLeft.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					instruction.command = InstructionCommand.LEFT;
					parent.notifyDataSetChanged();
					dialog.dismiss();
				}
			});

			ImageButton butRight = (ImageButton) dialog
					.findViewById(R.id.butRight);
			butRight.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					instruction.command = InstructionCommand.RIGHT;
					parent.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
			
			ImageButton butForward = (ImageButton) dialog
					.findViewById(R.id.butForward);
			butForward.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					instruction.command = InstructionCommand.FORWARD;
					parent.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
			
			ImageButton butStop = (ImageButton) dialog
					.findViewById(R.id.butStop);
			butStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					instruction.command = InstructionCommand.STOP;
					parent.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
			
			
			dialog.show();
			return true;
		}

	}

}
