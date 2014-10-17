/*
 * Copyright (C) 2012 Karen P. Tang, Sen Hirano
 * 
 * This file is part of the Estrellita project.
 * 
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of 
 * the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. If not, see
 * 				
 * 				http://www.gnu.org/licenses/
 * 
 */

/**
 * @author Karen P. Tang
 * @author Sen Hirano
 * 
 */

package edu.uci.ics.star.estrellita.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.TextboxDialog;
import edu.uci.ics.star.estrellita.customview.TextboxDialog.OnDialogResult;
import edu.uci.ics.star.estrellita.db.table.indicator.PostTable;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.object.User;
import edu.uci.ics.star.estrellita.object.indicator.Post;
import edu.uci.ics.star.estrellita.sharedprefs.api.PostApi;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;
import edu.uci.ics.star.estrellita.utils.WebUtils;

public class Wall extends TileActivity<Post> {
	private static final String GO_TO_LINK = "Go to the website";
	private static final String OPEN_DIALER = "Call the number";
	private static final int UPDATE_STATUS_ID = 0;
	public static final int MAKE_COMMENT = 2;
	public static final String LAST_TIMESTAMP = "last_viewed";

	private ListView homePostsPane;
	private int current;
	private static final int NUM_ENTRIES = 20;
	private PostTable postTable;
	private HomeWallListAdapter homeWallListAdapter;
	
	private long mLastViewed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.wall);

		// initialize header
		setActivityHeader("Inbox", true, Tile.WALL);
		setButtonHeader("add a new message");
		
		// get the timestamp of the last time you viewed the wall
		// we'll use this to bold the new messages
		mLastViewed = getIntent().getLongExtra(Wall.LAST_TIMESTAMP, -1);
		
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mAddMessage);

		openDatabase();
		postTable = (PostTable) mDatabase.getPostTable();

		homePostsPane = (ListView) findViewById(R.id.home_posts);
		homeWallListAdapter = new HomeWallListAdapter(this, new ArrayList<Post>());
		homePostsPane.setAdapter(homeWallListAdapter);

		homePostsPane.setDivider(null);

		homePostsPane.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				final Post post = homeWallListAdapter.getItem(position);
				List<String> extractUrls = WebUtils.extractUrls(post.getMsg());
				final String[] urlArray = new String[extractUrls.size()];
				extractUrls.toArray(urlArray);

				List<String> extractPhoneNumbers = StringUtils.extractPhoneNumbers(post.getMsg());
				final String[] phoneNumberArray = new String[extractPhoneNumbers.size()];
				extractPhoneNumbers.toArray(phoneNumberArray);
				
				if ((urlArray.length > 0) && (phoneNumberArray.length == 0)) { // post only has urls
					String[] builderOptions = new String[]{GO_TO_LINK, "Reply"};
					AlertDialog.Builder builder = new AlertDialog.Builder(Wall.this);
					builder.setTitle("Actions for post " + post.getMsg());
					builder.setItems(builderOptions, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialogInterface, int item) {
							switch(item){
							case 0:
								whichLinkPopup(urlArray);
								break;
							case 1:
								makeCommentOnPost(post);
								break;
							}

							return;
						}
					});
					builder.create().show();
				} 
				else if ((phoneNumberArray.length > 0) && (urlArray.length == 0)) { // post only has phone numbers
					String[] builderOptions = new String[]{OPEN_DIALER, "Reply"};
					AlertDialog.Builder builder = new AlertDialog.Builder(Wall.this);
					builder.setTitle("Actions for post " + post.getMsg());
					builder.setItems(builderOptions, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialogInterface, int item) {
							switch(item){
							case 0:
								whichPhoneNumberPopup(phoneNumberArray);
								break;
							case 1:
								makeCommentOnPost(post);
								break;
							}

							return;
						}
					});
					builder.create().show();
				}
				else if ((urlArray.length > 0) && (phoneNumberArray.length > 0)) { //post has both phone numbers and links
					String[] builderOptions = new String[]{OPEN_DIALER, GO_TO_LINK, "Reply"};
					AlertDialog.Builder builder = new AlertDialog.Builder(Wall.this);
					builder.setTitle("Actions for post " + post.getMsg());
					builder.setItems(builderOptions, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialogInterface, int item) {
							switch(item){
							case 0:
								whichPhoneNumberPopup(phoneNumberArray);
								break;
							case 1:
								whichLinkPopup(urlArray);
								break;
							case 2:
								makeCommentOnPost(post);
								break;
							}

							return;
						}
					});
					builder.create().show();
				} 
				else {
					makeCommentOnPost(post);
				}
			}
		});

		homePostsPane.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				try{
					AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

					Post post = homeWallListAdapter.getItem(info.position);
					menu.setHeaderTitle("Actions for post " + post.getMsg());  
					List<String> extractUrls = WebUtils.extractUrls(post.getMsg());
					List<String> extractPhoneNumbers = StringUtils.extractPhoneNumbers(post.getMsg());
					if(extractUrls.size() > 0) {
						menu.add(0,info.position, 0, GO_TO_LINK);  
					}
					if (extractPhoneNumbers.size() > 0) {
						menu.add(0,info.position, 0, OPEN_DIALER);
					}
					menu.add(0, info.position, 0, "Reply");
				} 
				catch(Exception e){

				}

			}
		});

		refresh(null);

	}

	@Override  
	public boolean onContextItemSelected(MenuItem item) {  
		Post post = homeWallListAdapter.getItem(item.getItemId());
		if (item.getTitle()==GO_TO_LINK) { 
			List<String> extractUrls = WebUtils.extractUrls(post.getMsg());
			final String[] urlArray = new String[extractUrls.size()];
			extractUrls.toArray(urlArray);
			whichLinkPopup(urlArray);
		} 
		else if (item.getTitle()==OPEN_DIALER) { 
			List<String> extractPhoneNumbers = StringUtils.extractPhoneNumbers(post.getMsg());
			final String[] phoneNumberArray = new String[extractPhoneNumbers.size()];
			extractPhoneNumbers.toArray(phoneNumberArray);
			whichLinkPopup(phoneNumberArray);
		}
		else if(item.getTitle() == "Reply") {
			makeCommentOnPost(post);
		}
		else {return false;}  
		return true;  
	}

	private void whichLinkPopup(final String[] urlArray) {
		if(urlArray.length > 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Pick a website to go to");
			builder.setItems(urlArray, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialogInterface, int item) {
					WebUtils.openUrlInBrowser(urlArray[item], Wall.this);
					return;
				}
			});
			builder.create().show();
		} else {
			WebUtils.openUrlInBrowser(urlArray[0], Wall.this);
		}
	}
	
	private void whichPhoneNumberPopup(final String[] phoneNumberArray) {
		if(phoneNumberArray.length > 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Pick a phone number to dial");
			builder.setItems(phoneNumberArray, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialogInterface, int item) {
					Utilities.callPhoneNumber(phoneNumberArray[item], Wall.this);
					return;
				}
			});
			builder.create().show();
		} else {
			Utilities.callPhoneNumber(phoneNumberArray[0], Wall.this);
		}
	}

	public Object getRestartData() {
		return Integer.valueOf(current);
	}

	public void refresh(final Object object) {
		homeWallListAdapter.empty();

		homePostsPane.setOnScrollListener(new OnScrollListener() {

			private int visibleThreshold = 5;
			private int currentPage = 0;
			private int previousTotal = 0;
			private boolean loading = true;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (loading) {
					if (totalItemCount > previousTotal) {
						loading = false;
						previousTotal = totalItemCount;
						currentPage++;
					}
				}
				if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
					// I load the next page of gigs using a background task,
					// but you can call any function here.
					next();
					loading = true;
				}
			}
		});
		if (object instanceof Integer) {
			Integer limit = (Integer) object;
			if (limit < 10) {
				limit = 10;
			}
			getPosts(0, limit);
		} else {
			current = 0;
			next();
		}

		homeWallListAdapter.notifyDataSetChanged();
	}

	public void next() {
		current = getPosts(current, NUM_ENTRIES);
	}

	private int getPosts(int start, int limit) {
		openDatabase();
		Post[] posts;
		postTable.prepareTable(mDatabase.getDb());

		posts = postTable.getWallPosts(mBaby.getId(), start, limit);
		start += posts.length;

		posts = postTable.getCommentsForPosts(mBaby.getId(), posts);

		for (Post post : posts) {
			homeWallListAdapter.addItem(post);
		}
		homeWallListAdapter.notifyDataSetChanged();
		return start;
	}

	private class HomeWallListAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<Post> posts;

		public HomeWallListAdapter(Context mContext, ArrayList<Post> posts) {
			super();
			this.mContext = mContext;
			this.posts = posts;
		}

		@Override
		public int getCount() {
			return posts.size();
		}

		@Override
		public Post getItem(int position) {
			return posts.get(position);
		}

		public void addItem(Post post) {
			posts.add(post);
		}

		@Override
		public long getItemId(int position) {
			return posts.get(position).getId();
		}

		public void empty() {
			posts.clear();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout rl;
			if (convertView == null) {
				rl = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.wall_item, parent, false);
			} else {
				rl = (RelativeLayout) convertView;
			}
			rl.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						v.setBackgroundColor(Color.YELLOW);
					} 
					else if (hasFocus) {
						v.setBackgroundColor(Color.WHITE);
					}
				}
			});
//			if(convertView == null || convertView.getId() != position) {
				Post post = posts.get(position);
				if (post.getCommentTo() != null) {
					rl.setPadding(40, rl.getPaddingTop(), rl.getPaddingRight(), rl.getPaddingBottom());
				} 
				else {
					rl.setPadding(10, rl.getPaddingTop(), rl.getPaddingRight(), rl.getPaddingBottom());
				}

				User user;
				try {
					openDatabase();
					user = mDatabase.getUserTable().getUser(post.getCommonData().getIdUser(), this.mContext);
				} 
				catch (NoUserException e) {
					user = new User();
				}

				((ImageView) rl.findViewById(R.id.home_wall__item_icon)).setImageBitmap(user.getConvertedImage());

				try {
					((TextView) rl.findViewById(R.id.home_wall__item_message)).setText(
							StringUtils.capitalize(user.getFirstName())
							+ " says "
							+ post.getModifiedMsg(mDatabase.getBabyTable().getBaby(post.getCommonData().getIdBaby(), this.mContext)));
					if (mLastViewed != -1) {
						Date d = new Date(mLastViewed);
						if (post.getDateTime().after(d)) {
							((TextView) rl.findViewById(R.id.home_wall__item_message)).setTypeface(null, Typeface.BOLD);
						}
						else {
							((TextView) rl.findViewById(R.id.home_wall__item_message)).setTypeface(null, Typeface.NORMAL);
						}
					}
					else {
						((TextView) rl.findViewById(R.id.home_wall__item_message)).setTypeface(null, Typeface.NORMAL);
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}

				((TextView) rl.findViewById(R.id.home_wall__item_info)).setText(DateUtils.formatReadableTimestamp(post.getDateTime()));
				rl.setId(position);
				// ((TextView)
				// rl.findViewById(R.id.pl_title)).setText(p.getTitle());
				// ((TextView)
				// rl.findViewById(R.id.pl_metadata)).setText(p.getTitle());
//			}

			return rl;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case UPDATE_STATUS_ID:
			return new TextboxDialog.Builder(this)
			.setTitle(getString(R.string.update_status))
			.setPositiveButton(new OnDialogResult() {
				@Override
				public void finish(String text) {
					if (text.length() > 0) {
						updateStatusTextView(text, EstrellitaTiles.getParentUser(Wall.this).getCurrentKidIndex());
						openDatabase();
						mDatabase.insertSingle(new Post(EstrellitaTiles.getParentUser(Wall.this), text));
						Wall.this.refresh(null);
						updateWidget();
					}
				}
			})
			.setNegativeButton(new OnDialogResult() {
				@Override
				public void finish(String text) {
				}
			})
			.create();
		case MAKE_COMMENT:
			return new TextboxDialog.Builder(this)
			.setTitle(getString(R.string.comment_on) + "\n" + PostApi.getCommentPost(this))
			.setPositiveButton(new OnDialogResult() {
				@Override
				public void finish(String text) {
					if (text.length() > 0) {
						int pcommid = PostApi.getCommentId(Wall.this);
						int pcommidL = PostApi.getCommentLocalId(Wall.this);
						// if pcommid is > 0, and the local number comes up 0 (happens when there isn't any)
						if (pcommid >= 0 && pcommidL == 0) {
							// do nothing because we're storing pcommid
						} 
						else if (pcommidL > 0) {
							// this next one is trickier since we're not certain whether
							// this filters for -1 and 0 not found
							pcommid = -1 * pcommidL;
						}
						openDatabase();
						mDatabase.insertSingle(new Post(EstrellitaTiles.getParentUser(Wall.this), text, pcommid));

						PostApi.clearCommentData(Wall.this);

						Wall.this.refresh(null);
						updateWidget();
					}
				}
			})
			.setNegativeButton(new OnDialogResult() {
				@Override
				public void finish(String text) {
				}
			})
			.create();
		}
		return onCreateDialog(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		dialog.setCanceledOnTouchOutside(true);
		switch (id) {
		case MAKE_COMMENT:
			String newTitle = PostApi.getCommentPost(this);
			if (newTitle.length() > 27) {
				newTitle = newTitle.substring(0, 24) + "...";
			}
			dialog.setTitle(getString(R.string.comment_on) + "\n" + newTitle);
			break;
		default:
			super.onPrepareDialog(id, dialog);
			break;
		}
		// this is called after oncreatedialog
	}

	private void updateStatusTextView(String newStatus, int childIndex) {
		// ((TextView)
		// flipper.getChildAt(childIndex).findViewById(R.id.tvUpdate)).setText(newStatus);
	}

	private void updateWidget() {
		Utilities.updateWidget(Wall.this);
	}

	private void makeCommentOnPost(Post post) {
		PostApi.putCommentdata(Wall.this, post.getId(), post.getMsg(), post.getLocalId());
		showDialog(MAKE_COMMENT);
	}

	View.OnClickListener mAddMessage = new OnClickListener() {
		public void onClick(View v) {
			showDialog(UPDATE_STATUS_ID);
		}
	};

}