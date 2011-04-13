package org.opensatnav.android.contribute.util.constants;

public class ContributeConstants {
	public static int getActionFromMenuId(int menuId) {
        switch (menuId) {
          case ContributeConstants.MENU_SEND_TO_OSM:
            return ContributeConstants.SEND_TO_OSM_DIALOG;
          case ContributeConstants.MENU_EDIT:
            return ContributeConstants.EDIT_DETAILS;
          case ContributeConstants.MENU_DELETE:
            return ContributeConstants.DELETE_TRACK;
          case ContributeConstants.MENU_SHARE_LINK:
            return ContributeConstants.SHARE_LINK;
          case ContributeConstants.MENU_SHARE_KML_FILE:
            return ContributeConstants.SHARE_KML_FILE;
          case ContributeConstants.MENU_SHARE_GPX_FILE:
            return ContributeConstants.SHARE_GPX_FILE;
          case ContributeConstants.MENU_SHARE_CSV_FILE:
            return ContributeConstants.SHARE_CSV_FILE;
          case ContributeConstants.MENU_SAVE_GPX_FILE:
            return ContributeConstants.SAVE_GPX_FILE;
          case ContributeConstants.MENU_SAVE_KML_FILE:
            return ContributeConstants.SAVE_KML_FILE;
          case ContributeConstants.MENU_SAVE_CSV_FILE:
            return ContributeConstants.SAVE_CSV_FILE;
          case ContributeConstants.MENU_CLEAR_MAP:
            return ContributeConstants.CLEAR_MAP;
          default:
            return -1;
        }
      }
	
	public static final int MENU_EDIT = 100;
    public static final int MENU_DELETE = 101;
    public static final int MENU_SEND_TO_OSM = 102;
    public static final int MENU_SHARE = 103;
    public static final int MENU_SHOW = 104;
    public static final int MENU_SHARE_LINK = 200;
    public static final int MENU_SHARE_GPX_FILE = 201;
    public static final int MENU_SHARE_KML_FILE = 202;
    public static final int MENU_SHARE_CSV_FILE = 203;
    public static final int MENU_WRITE_TO_SD_CARD = 204;
    public static final int MENU_SAVE_GPX_FILE = 205;
    public static final int MENU_SAVE_KML_FILE = 206;
    public static final int MENU_SAVE_CSV_FILE = 207;
    public static final int MENU_CLEAR_MAP = 208;
    
    
    
    public static final int GET_LOGIN = 0;
    public static final int GET_MAP = 1;
    public static final int CREATE_MAP = 2;
    public static final int SHOW_TRACK = 3;
    public static final int ADD_LIST = 4;
    public static final int FEATURE_DETAILS = 5;
    public static final int DELETE_TRACK = 11;
    public static final int SEND_TO_OSM = 12;
    public static final int SEND_TO_OSM_DIALOG = 13;
    public static final int SHARE_LINK = 14;
    public static final int SHARE_GPX_FILE = 15;
    public static final int SHARE_KML_FILE = 16;
    public static final int SHARE_CSV_FILE = 17;
    public static final int EDIT_DETAILS = 18;
    public static final int SAVE_GPX_FILE = 19;
    public static final int SAVE_KML_FILE = 20;
    public static final int SAVE_CSV_FILE = 21;
    public static final int CLEAR_MAP = 22;
    public static final int SHOW_WAYPOINT = 23;
    public static final int EDIT_WAYPOINT = 24;
    
    public static final int DIALOG_SEND_TO_OSM = 400;
}
