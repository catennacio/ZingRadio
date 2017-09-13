package com.friends.zingradio.util;

import java.util.Calendar;
import java.util.Hashtable;

import android.content.Context;
import android.util.Log;

import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.entity.Station;

public class StationSuggestion
{
    public static final String STATION_GOOD_MORNING_KEY                  = "STATION_GOOD_MORNING_KEY";//Good Morning - 5AM-9AM, weekday
    public static final String STATION_GOOD_NIGHT_KEY                    = "STATION_GOOD_NIGHT_KEY";//Good Night - 8PM-1AM, weekday
    public static final String STATION_GIAI_DIEU_TINH_YEU_KEY            = "STATION_GIAI_DIEU_TINH_YEU_KEY";//Giai Dieu Tinh Yeu - Sat night
    public static final String STATION_COFFEE_SHOP_KEY                   = "STATION_COFFEE_SHOP_KEY";//Coffee Shop - Sun morning, weekday morning
    public static final String STATION_CAKHUC_MOI_KEY                    = "STATION_CAKHUC_MOI_KEY";//Ca khuc moi - All other time
    public static final String STATION_CHUYEN_TAU_AM_NHAC_KEY            = "STATION_CHUYEN_TAU_AM_NHAC_KEY";//Chuyen tau am nhac - 8PM toi thu 4
    public static final String STATION_DIEM_TIN_GIAI_TRI_KEY             = "STATION_DIEM_TIN_GIAI_TRI_KEY";//Diem tin giai tri - 8PM toi thu 6
    public static final String STATION_SO_TAY_CAM_XUC_KEY                = "STATION_SO_TAY_CAM_XUC_KEY";//So tay cam xuc - 8PM toi thu 7    
    public static final String STATION_NHIP_DIEU_DIEN_TU_KEY             = "STATION_NHIP_DIEU_DIEN_TU_KEY";//Nhip dieu dien tu
    public static final String STATION_GIAI_DIEU_A_CHAU_KEY              = "STATION_GIAI_DIEU_A_CHAU_KEY";//Giai dieu A Chau - 8PM toi thu CN
    public static final String STATION_PARTY_SONG_KEY                    = "STATION_PARTY_SONG_KEY";//party song - friday night
    public static final String STATION_GIA_DINH_TOI_KEY                  = "STATION_GIA_DINH_TOI_KEY";//Toi CN
    public static final String STATION_NHAC_BAT_HU_KEY                   = "STATION_NHAC_BAT_HU_KEY";
    public static final String STATION_KET_NOI_TRAI_TIM_KEY              = "STATION_KET_NOI_TRAI_TIM_KEY";
    public static final String STATION_HOA_TAU_KEY                       = "STATION_HOA_TAU_KEY";//play at 12 noon
    public static final String STATION_THEO_YEU_CAU_KEY                  = "STATION_THEO_YEU_CAU_KEY";
    public static final String STATION_CONFESSIONS_KEY                   = "STATION_CONFESSIONS_KEY";//20h thu hai
    public static final String STATION_US_UK_KEY                         = "STATION_US_UK_KEY";
    public static final String STATION_VPOP_KEY                          = "STATION_VPOP_KEY";
    public static final String STATION_TAP_CHI_SHOW_BIZ_KEY              = "STATION_TAP_CHI_SHOW_BIZ_KEY";//20h thu nam
    
    
    public static final String STATION_GOOD_MORNING_VALUE                = "LnJnyknNBNALcVlTZDJTDmLn";
    public static final String STATION_GOOD_NIGHT_VALUE                  = "LHcGtLnNdNALxVVykFxtDGZH";
    public static final String STATION_GIAI_DIEU_TINH_YEU_VALUE          = "knJmtLHNVsALxdvykbxTvnLG";
    public static final String STATION_COFFEE_SHOP_VALUE                 = "ZGJHykHaVNSZingTkbxyDHZG";
    public static final String STATION_CAKHUC_MOI_VALUE                  = "LnJHyLHadNALxxhTLDxyFnZn";
    public static final String STATION_CHUYEN_TAU_AM_NHAC_VALUE          = "kHcHyLGNBsAvnVEyLvJtFHLm";
    public static final String STATION_DIEM_TIN_GIAI_TRI_VALUE           = "LGcGyLGNdNlbnBVyLDJyDGLG";
    public static final String STATION_SO_TAY_CAM_XUC_VALUE              = "knJntkHsBslbGVstLvxyDnkn";    
    public static final String STATION_NHIP_DIEU_DIEN_TU_VALUE           = "knxnTLmNBazFGdXyLvctvHLH";
    public static final String STATION_GIAI_DIEU_A_CHAU_VALUE            = "ZnJmTkmsBNADnVzTLDJtFnLm";
    public static final String STATION_PARTY_SONG_VALUE                  = "kGJHyknsBNSLiLDTLDcTbnLH";
    public static final String STATION_GIA_DINH_TOI_VALUE                = "LHxGtLGNdaAkRAFtkDxybHLm";
    public static final String STATION_NHAC_BAT_HU_VALUE                 = "LHJGyZmNdszZxdGTLbJybmLn";
    public static final String STATION_KET_NOI_TRAI_TIM_VALUE            = "kHxnykHsdNSFmQQykDctFHLm";
    public static final String STATION_HOA_TAU_VALUE                     = "kncnyLGsdazLEmNyZDxtFnLG";
    public static final String STATION_THEO_YEU_CAU_VALUE                = "kGxmTZnadNlZJdZykFxyFnLH";
    public static final String STATION_CONFESSIONS_VALUE                 = "LGJHykGadNSbnQNyLvcyvmkH";
    public static final String STATION_US_UK_VALUE                       = "LHJHtZHadNAkcvsyLDJtbnkn";
    public static final String STATION_VPOP_VALUE                        = "ZmJHTLHadalkJvQTLvctvGLG";
    public static final String STATION_TAP_CHI_SHOW_BIZ_VALUE            = "ZmJGtLHNdaAbHpiyLbJybGLH";
    
    
    
    public static final String STATION_DEFAULT_KEY                       = STATION_CONFESSIONS_KEY;
    private static final String TAG = StationSuggestion.class.getSimpleName();
    
    private static Hashtable<String, String> mSuggestedStations = new Hashtable<String, String>();
    
    public static void buildSuggestedStation()
    {
        mSuggestedStations.put(STATION_GOOD_MORNING_KEY, STATION_GOOD_MORNING_VALUE);
        mSuggestedStations.put(STATION_GOOD_NIGHT_KEY, STATION_GOOD_NIGHT_VALUE);
        mSuggestedStations.put(STATION_GIAI_DIEU_TINH_YEU_KEY, STATION_GIAI_DIEU_TINH_YEU_VALUE);
        mSuggestedStations.put(STATION_COFFEE_SHOP_KEY, STATION_COFFEE_SHOP_VALUE);
        mSuggestedStations.put(STATION_CAKHUC_MOI_KEY, STATION_CAKHUC_MOI_VALUE);
        mSuggestedStations.put(STATION_CHUYEN_TAU_AM_NHAC_KEY, STATION_CHUYEN_TAU_AM_NHAC_VALUE);
        mSuggestedStations.put(STATION_DIEM_TIN_GIAI_TRI_KEY, STATION_DIEM_TIN_GIAI_TRI_VALUE);
        mSuggestedStations.put(STATION_SO_TAY_CAM_XUC_KEY, STATION_SO_TAY_CAM_XUC_VALUE);
        mSuggestedStations.put(STATION_NHIP_DIEU_DIEN_TU_KEY, STATION_NHIP_DIEU_DIEN_TU_VALUE);
        mSuggestedStations.put(STATION_GIAI_DIEU_A_CHAU_KEY, STATION_GIAI_DIEU_A_CHAU_VALUE);
        mSuggestedStations.put(STATION_PARTY_SONG_KEY, STATION_PARTY_SONG_VALUE);
        mSuggestedStations.put(STATION_GIA_DINH_TOI_KEY, STATION_GIA_DINH_TOI_VALUE);
        mSuggestedStations.put(STATION_NHAC_BAT_HU_KEY, STATION_NHAC_BAT_HU_VALUE);
        mSuggestedStations.put(STATION_KET_NOI_TRAI_TIM_KEY, STATION_KET_NOI_TRAI_TIM_VALUE);
        mSuggestedStations.put(STATION_HOA_TAU_KEY, STATION_HOA_TAU_VALUE);
        mSuggestedStations.put(STATION_THEO_YEU_CAU_KEY, STATION_THEO_YEU_CAU_VALUE);
        mSuggestedStations.put(STATION_CONFESSIONS_KEY, STATION_CONFESSIONS_VALUE);
        mSuggestedStations.put(STATION_US_UK_KEY, STATION_US_UK_VALUE);
        mSuggestedStations.put(STATION_VPOP_KEY, STATION_VPOP_VALUE);
    }
    
    public static Hashtable<String, String> getSuggestedStation()
    {
        return mSuggestedStations;
    }
    
    private static String getSuggestStationForMonday()
    {
        String s = null;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        {
            switch(hour)
            {
                case 0: case 1: case 2: case 3: case 4: case 21: case 22: case 23:
                {
                    s = mSuggestedStations.get(STATION_GOOD_NIGHT_KEY);
                    break;
                }
                case 5: case 6: case 7: case 8: case 9:
                {
                    s = mSuggestedStations.get(STATION_GOOD_MORNING_KEY);
                    break;
                }
                case 10: case 11: case 18: case 19:
                {
                    s = mSuggestedStations.get(STATION_CAKHUC_MOI_KEY);
                    break;
                }
                case 12:
                {
                    s = mSuggestedStations.get(STATION_HOA_TAU_KEY);
                    break;
                }
                case 13: case 14: case 15:
                {
                    s = mSuggestedStations.get(STATION_CAKHUC_MOI_KEY);
                    break;
                }
                case 16: case 17:
                {
                    s = mSuggestedStations.get(STATION_COFFEE_SHOP_KEY);
                    break;
                }
                case 20:
                {
                    s = mSuggestedStations.get(STATION_CONFESSIONS_KEY);
                    break;
                }
                default:
                {
                    s = mSuggestedStations.get(STATION_DEFAULT_KEY);
                    break;   
                }
            }
        }
        
        return s;
    }
    
    private static String getSuggestStationForTuesday()
    {
        String s = null;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        {
            switch(hour)
            {
                case 0: case 1: case 2: case 3: case 4: case 21: case 22: case 23:
                {
                    s = mSuggestedStations.get(STATION_GOOD_NIGHT_KEY);
                    break;
                }
                case 5: case 6: case 7: case 8: case 9:
                {
                    s = mSuggestedStations.get(STATION_GOOD_MORNING_KEY);
                    break;
                }
                case 10: case 11: case 13: case 14:
                {
                    s = mSuggestedStations.get(STATION_CAKHUC_MOI_KEY);
                    break;
                }
                case 12:
                {
                    s = mSuggestedStations.get(STATION_HOA_TAU_KEY);
                    break;
                }
                case 15:
                {
                    s = mSuggestedStations.get(STATION_COFFEE_SHOP_KEY);
                    break;
                }
                case 16: case 17:
                {
                    s = mSuggestedStations.get(STATION_CAKHUC_MOI_KEY);
                    break;
                }
                default:
                {
                    s = mSuggestedStations.get(STATION_DEFAULT_KEY);
                    break;   
                }
            }
        }
        
        return s;
    }
    
    private static String getSuggestStationForWednesday()
    {
        String s = null;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        {
            switch(hour)
            {
                case 0: case 1: case 2: case 3: case 4: case 22: case 23:
                {
                    s = mSuggestedStations.get(STATION_GOOD_NIGHT_KEY);
                    break;
                }
                case 5: case 6: case 7: case 8: case 9:
                {
                    s = mSuggestedStations.get(STATION_GOOD_MORNING_KEY);
                    break;
                }
                case 10: case 11:
                {
                    s = mSuggestedStations.get(STATION_CAKHUC_MOI_KEY);
                    break;
                }
                case 12:
                {
                    s = mSuggestedStations.get(STATION_HOA_TAU_KEY);
                    break;
                }
                case 13: case 14: case 15:
                {
                    s = mSuggestedStations.get(STATION_US_UK_KEY);
                }
                case 16: case 17:
                {
                    s = mSuggestedStations.get(STATION_COFFEE_SHOP_KEY);
                    break;
                }
                case 20: case 21:
                {
                    s = mSuggestedStations.get(STATION_CHUYEN_TAU_AM_NHAC_KEY);
                    break;
                }
                default:
                {
                    s = mSuggestedStations.get(STATION_CAKHUC_MOI_KEY);
                    break;   
                }
            }
        }
        
        return s;
    }
    
    private static String getSuggestStationForThursday()
    {
        String s = null;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        {
            switch(hour)
            {
                case 0: case 1: case 2: case 3: case 4: case 22: case 23:
                {
                    s = mSuggestedStations.get(STATION_GOOD_NIGHT_KEY);
                    break;
                }
                case 5: case 6: case 7: case 8: case 9:
                {
                    s = mSuggestedStations.get(STATION_GOOD_MORNING_KEY);
                    break;
                }
                case 10: case 11: case 13: case 14: case 15:
                {
                    s = mSuggestedStations.get(STATION_CAKHUC_MOI_KEY);
                    break;
                }
                case 12:
                {
                    s = mSuggestedStations.get(STATION_HOA_TAU_KEY);
                    break;
                }
                case 16: case 17:
                {
                    s = mSuggestedStations.get(STATION_COFFEE_SHOP_KEY);
                    break;
                }
                case 20:
                {
                    s = mSuggestedStations.get(STATION_TAP_CHI_SHOW_BIZ_KEY);
                    break;
                }
                case 21:
                {
                    s = mSuggestedStations.get(STATION_CHUYEN_TAU_AM_NHAC_KEY);
                    break;
                }
                default:
                {
                    s = mSuggestedStations.get(STATION_DEFAULT_KEY);
                    break;   
                }
            }
        }
        
        return s;
    }
    
    private static String getSuggestStationForFriday()
    {
        String s = null;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        {
            switch(hour)
            {
                case 0: case 1: case 23:
                {
                    s = mSuggestedStations.get(STATION_PARTY_SONG_KEY);
                    break;
                }
                case 2: case 3: case 4:
                {
                    s = mSuggestedStations.get(STATION_GOOD_NIGHT_KEY);
                    break;
                }
                case 5: case 6: case 7: case 8: case 9:
                {
                    s = mSuggestedStations.get(STATION_GOOD_MORNING_KEY);
                    break;
                }
                case 10: case 11: case 13: case 14: case 15:
                {
                    s = mSuggestedStations.get(STATION_CAKHUC_MOI_KEY);
                    break;
                }
                case 12:
                {
                    s = mSuggestedStations.get(STATION_HOA_TAU_KEY);
                    break;
                }
                case 16: case 17: 
                {
                    s = mSuggestedStations.get(STATION_COFFEE_SHOP_KEY);
                    break;
                }
                case 18: case 19:
                {
                    s = mSuggestedStations.get(STATION_THEO_YEU_CAU_KEY);
                    break;
                }
                case 20: case 21:
                {
                    s = mSuggestedStations.get(STATION_DIEM_TIN_GIAI_TRI_KEY);
                    break;
                }
                default:
                {
                    s = mSuggestedStations.get(STATION_NHAC_BAT_HU_KEY);
                    break;   
                }
            }
        }
        
        return s;
    }
    
    private static String getSuggestStationForSaturday()
    {
        String s = null;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        {
            switch(hour)
            {
                case 0: case 22: case 23:
                {
                    s = mSuggestedStations.get(STATION_PARTY_SONG_KEY);
                    break;
                }
                case 1: case 2: case 3: case 4: case 5:
                {
                    s = mSuggestedStations.get(STATION_GOOD_NIGHT_KEY);
                    break;
                }
                case 6: case 7: case 8:
                {
                    s = mSuggestedStations.get(STATION_GOOD_MORNING_KEY);
                    break;
                }
                case 9: case 10: case 11:
                {
                    s = mSuggestedStations.get(STATION_GIAI_DIEU_TINH_YEU_KEY);
                    break;
                }
                case 12:
                {
                    s = mSuggestedStations.get(STATION_GIAI_DIEU_TINH_YEU_KEY);
                    break;
                }
                case 13: case 14:
                {
                    s = mSuggestedStations.get(STATION_DIEM_TIN_GIAI_TRI_KEY);
                    break;
                }
                case 15: case 16:  
                {
                    s = mSuggestedStations.get(STATION_COFFEE_SHOP_KEY);
                    break;
                }
                case 17: case 18: case 19: case 20: case 21:
                {
                    s = mSuggestedStations.get(STATION_GIAI_DIEU_TINH_YEU_KEY);
                    break;
                }
                default:
                {
                    s = mSuggestedStations.get(STATION_DEFAULT_KEY);
                    break;   
                }
            }
        }
        
        return s;
    }
    
    private static String getSuggestStationForSunday()
    {
        String s = null;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        {
            switch(hour)
            {
                case 0:
                {
                    s = mSuggestedStations.get(STATION_PARTY_SONG_KEY);
                    break;
                }
                case 1: case 2: case 3: case 4: case 5: case 22: case 23:
                {
                    s = mSuggestedStations.get(STATION_GOOD_NIGHT_KEY);
                    break;
                }
                case 6: case 7: case 8: case 9:
                {
                    s = mSuggestedStations.get(STATION_GOOD_MORNING_KEY);
                    break;
                }
                case 10: case 11: case 12: case 13:
                {
                    s = mSuggestedStations.get(STATION_NHAC_BAT_HU_KEY);
                    break;
                }
                case 14: case 15: case 16:
                {
                    s = mSuggestedStations.get(STATION_COFFEE_SHOP_KEY);
                    break;
                }
                case 17: case 18:
                {
                    s = mSuggestedStations.get(STATION_NHAC_BAT_HU_KEY);
                    break;
                }
                case 19: case 21:
                {
                    s = mSuggestedStations.get(STATION_GIA_DINH_TOI_KEY);
                    break;
                }
                case 20:
                {
                    s = mSuggestedStations.get(STATION_NHIP_DIEU_DIEN_TU_KEY);
                    break;
                }
                default:
                {
                    s = mSuggestedStations.get(STATION_COFFEE_SHOP_KEY);
                    break;   
                }
            }
        }
        
        return s;
    }
    
    public static String getSuggestStationServerId()
    {
        String s = null;
        Calendar c = Calendar.getInstance();        
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        switch(dayOfWeek)
        {
            case Calendar.MONDAY:
            {
                s = getSuggestStationForMonday();
                break;
            }
            case Calendar.TUESDAY:
            {
                s = getSuggestStationForTuesday();
                break;
            }
            case Calendar.WEDNESDAY:
            {
                s = getSuggestStationForWednesday();
                break;
            }
            case Calendar.THURSDAY:
            {
                s = getSuggestStationForThursday();
                break;
            }
            case Calendar.FRIDAY:
            {
                s = getSuggestStationForFriday();
                break;
            }
            case Calendar.SATURDAY:
            {
                s = getSuggestStationForSaturday();
                break;
            }
            case Calendar.SUNDAY:
            {
                s = getSuggestStationForSunday();
                break;
            }
        }
        
        if(s == null)
            s = STATION_CAKHUC_MOI_VALUE;
        
        return s;
    }
    
    public static Station getSuggestedStation(Context ctx)
    {
        ZingRadioApplication app = ((ZingRadioApplication)ctx.getApplicationContext());
        app.loadAll();
        String serverId = getSuggestStationServerId();
        Log.d(TAG, "serverId=" + serverId);
        if(serverId == null) serverId = mSuggestedStations.get(STATION_DEFAULT_KEY);
        Station st = app.getRadio().getStation(serverId);
        Log.d(TAG, "Suggested station name=" + st.getName());
        return st;
    }
}
