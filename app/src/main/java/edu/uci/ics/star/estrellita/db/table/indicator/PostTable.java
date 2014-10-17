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

package edu.uci.ics.star.estrellita.db.table.indicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.Post;
import edu.uci.ics.star.estrellita.utils.DateUtils;

// idUser = baby and id_commenter is the parent
public class PostTable extends IndicatorTable {
	/*
CREATE TABLE IF NOT EXISTS `post` (
  `idPost` int(11) NOT NULL AUTO_INCREMENT,
  `msg` varchar(200) NOT NULL,
  `dateTime` datetime NOT NULL,
  `idUser` int(11) NOT NULL,
  `idBaby` int(11) NOT NULL,
  `type` enum('P','C','VC') NOT NULL,
  `flag` enum('urgent','warning') NOT NULL,
  PRIMARY KEY (`idPost`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=249 ;
	 */

	public static final String MSG = "msg";
	public static final String COMMENT_TO = "comment_to";
	public static final String TYPE = "type";

	public PostTable() {
		super("Post", ",'" + MSG + "' varchar(160)" +
				",'" + TYPE + "' varchar(160)" +
				",'" + COMMENT_TO + "' int(11)");
		this.setDuration(DateUtils.DAY);

		setOptional(false);
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.estrellita.db.tables.GenericTable#addDataToCV(ics.uci.edu.star.estrellita.object.Indicator, android.content.ContentValues)
	 */
	@Override
	public boolean addDataToCV(Indicator indicator, ContentValues cv)
	throws WrongIndicatorException {
		if(indicator instanceof Post){
			Post post = (Post) indicator;

			cv.put(MSG, post.getMsg());
			cv.put(TYPE, post.getType());

			// complicated
			Integer commentTo;


			// if commentTo is null that means that this should be a new post, and commentTo should be null
			if(post.getCommentTo() == null){
				commentTo = null;
			} 
			// if commentTo != null that means that the post this comment is to is a comment to another post
			// so we are setting the target post to the post at the root of the comment thread
			else if((commentTo = getCommentToOfComment(post.getCommentTo())) != null){
			}
			// if commentTo is null that means that there is no root node
			// but this checks to see if it is pointing at a local, and tries to see if it is synced 
			// if it is, then change the commentTo to be the realId
			else if(post.getCommentTo() < 1){
				// find check to see if that local id is synced
				// multiply by negative 1 to get the positive localId
				Cursor cursor2 = getCursor(WHERE + IndicatorTable.LOCAL_ID + "==" + -1*post.getCommentTo() + " AND " 
						+ IndicatorTable.SYNCED + "==1", 0, 1, null);
				Cursor cursor3 = null;
				try {
					// if it is
					if(cursor2.moveToFirst()){
						// make commentTo the officialId instead of the local
						commentTo = cursor2.getInt(cursor2.getColumnIndex(IndicatorTable.ID));
					} else {
						cursor2.close();
						cursor3 = getCursor(WHERE + IndicatorTable.ID + "==" + post.getCommentTo() + " AND " 
								+ IndicatorTable.SYNCED + "==0", 0, 1, null);
						if(cursor3.moveToFirst()){
							commentTo = cursor3.getInt(cursor3.getColumnIndex(IndicatorTable.ID));
						}
					}
				} finally {
					if(cursor2 != null)
						cursor2.close();
					if (cursor3 != null) {
						cursor3.close();
					}
				}
			} 
			// this means that post.commentTo is already pointing at the real id, so just return it
			else{
				commentTo = post.getCommentTo();
			}
			cv.put(COMMENT_TO, commentTo);
			post.setCommentTo(commentTo);
			return true;
		}
		throw new WrongIndicatorException();
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.db.table.IndicatorTable#getIndicatorArray(android.database.Cursor)
	 */
	public Indicator[] getIndicatorArray(Cursor cursor) {
		Post[] results = new Post[cursor.getCount()];
		Post d;
		int i;
		for(int k = 0; k < cursor.getCount(); k ++){
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();

			d = new Post(this.getCommonData(cursor),  cursor.getString(i++),
					cursor.getString(i++), null);
			if(!cursor.isNull(i)){
				d.setCommentTo(cursor.getInt(i));
			}
			results[k] = d;
		}
		return results;
	}

	public Post getMostRecentPost(int idBaby){
		try{
			return (Post) getSetOfIndicators( " " + WHERE + " " + BABY_ID+" = " + idBaby + " AND " + COMMENT_TO + " IS NULL ", 0, 1, IndicatorTable.CREATED_AT + " DESC")[0];
		} catch (ArrayIndexOutOfBoundsException aioobe){
			return null;
		}
	}

	public Class<?> getIndicatorClass() {
		return Post.class;
	}

	public int getNumCommentsForPost(int postId) {
		return count(WHERE + COMMENT_TO + "=" + postId);
	}

	public int getNumOfVirtualCoachPosts(int idBaby) {
		return count(WHERE + BABY_ID + "==" + idBaby + " AND " + TYPE + "=='VC'");
	}

	public Post[] getWallPosts(int idUser, int current, int numEntries) {
		return getPosts(idUser, null, current, numEntries);
	}

	public Post[] getCommentsForPosts(int idBaby, Post[] posts) {
		ArrayList<Post> postsList =  new ArrayList<Post>();
		Integer commentId;
		for (Post post: posts) {
			postsList.add(post);
			if(post.getId() == null){
				commentId = post.getLocalId()*-1;
			} else {
				commentId = post.getId();
			}
			List<Post> asList = Arrays.asList(getPosts(idBaby, commentId, 0,-1));
			//			Collections.reverse(asList);
			postsList.addAll(asList);
		}
		return postsList.toArray(posts);
	}

	public Integer getCommentToOfComment(Integer postId){
		String targetSearch;
		if(postId == null){
			return null;
		}
		// if the post is negative then it is pointing to a local id
		if(postId < 0){
			targetSearch = IndicatorTable.LOCAL_ID;
			postId = -1*postId;
		} else{
			targetSearch = IndicatorTable.ID;
		}
		Cursor c = null;
		
		
		Integer commentTo = null;
		try {
			c = getCursor(WHERE + targetSearch + "="+postId);
			Post comment = ((Post[])getIndicatorArray(c))[0];
			commentTo = comment.getCommentTo();

		} finally {
			if(c != null)
				c.close();
		}
		return commentTo;
	}

	private Post[] getPosts(int idBaby, Integer commentTo, int current, int numEntries) {
		String whereString;
		if(commentTo == null){
			whereString = "is null";
		} else{
			whereString = "=" + commentTo;
		}
		return (Post[])getSetOfIndicators(" " + WHERE + " " + IndicatorTable.BABY_ID + "="+
				idBaby + " AND " + COMMENT_TO + " " + whereString, current, numEntries, IndicatorTable.CREATED_AT + " DESC");
	} 

	/**
	 * @param cv
	 * @param oldId
	 */
	@Override
	public Integer update(ContentValues cv){
		Integer localId = cv.getAsInteger(IndicatorTable.LOCAL_ID);
		getDb().update(this.getTableName(), cv, IndicatorTable.LOCAL_ID + "=?",
				new String[]{ Integer.toString(localId)});
//		Utilities.println("UPDATED ROWS: " + getDb().update(this.getTableName(), cv, IndicatorTable.LOCAL_ID + "=?",
//				new String[]{ Integer.toString(localId)}));
		ContentValues newCv = new ContentValues();
		newCv.put(COMMENT_TO, cv.getAsInteger(IndicatorTable.ID));

		// at this point the comment to field in cv should already be negative
		// (indicates a pointer to localId) and we're only interested in setting those
		getDb().update(this.getTableName(), newCv, COMMENT_TO + "=?", new String[]{ Integer.toString(localId*-1)});
//		Utilities.println("UPDATED POST COMMENT ROWS: " + getDb().update(this.getTableName(), newCv, COMMENT_TO + "=?",
//				new String[]{ Integer.toString(localId*-1)}));
		return localId;
	}


	public Integer getPostCountSince(int idBaby, Date d) {
		String where = " WHERE " + BABY_ID + "==" + idBaby + " AND " + CREATED_AT + " >= " + d.getTime();
		Post[] posts = (Post[]) getSetOfIndicators(where, -1, 0);
		return posts.length;
	}

}
