package com.example.lets_findus.utilities;

import android.net.Uri;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.LocalDate;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilFunction {
    //funzione che restituisce una data di n giorni indietro rispetto a quella passata in input
    private static Date subtractDays(Date d, int numDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DAY_OF_MONTH, -numDays);
        return calendar.getTime();
    }
    //funzione che restituisce true se la data è contenuta nel range di ore specificato, false altrimenti
    //lowerBound e upperBound sono double che rappresentano un ora, ad esempio 15:30 equivalgono a 15,3
    private static boolean isInHourRange(Date d, double lowerBound, double upperBound){
        DateTime date = new DateTime(d);
        int hour = date.getHourOfDay();
        int minutes = date.getMinuteOfHour();
        double hourAndMinutes = hour + (double)minutes/100;
        return (hourAndMinutes >= lowerBound && hourAndMinutes <= upperBound);
    }
    //funzione che restituisce true se la data è contenuta nel range di anni specificato, false altrimenti
    private static boolean isInYearRange(int yearOfBirth, int lowerBound, int upperBound){
        int currentYear = LocalDate.now().getYear();
        if(upperBound == 0){
            return (currentYear-yearOfBirth) >= lowerBound;
        }
        return (currentYear-yearOfBirth) >= lowerBound && (currentYear-yearOfBirth) <= upperBound;
    }
    //funzione che filtra i meetings contenuti nella lista allMeeting andando a modificare il contenuto della lista meetingsToFilter secondo le relative filterOptions
    public static void filterItems(List<MeetingPerson> meetingsToFilter, List<MeetingPerson> allMeetings, Map<String, String> filterOptions) {
        //riporto allo stato iniziale la lista di meetingsToFilter
        meetingsToFilter.clear();
        meetingsToFilter.addAll(allMeetings); 
        boolean needsToRemove = false;
        Iterator<MeetingPerson> mpIterator = meetingsToFilter.iterator();
        //per ogni elemento contenuto nella lista di meetings controllo se rispetta tutti i filtri altrimenti lo rimuovo dalla lista
        while (mpIterator.hasNext()) {
            needsToRemove = false;
            MeetingPerson elem = mpIterator.next();
            switch (filterOptions.get("sex")) {
                case "Maschio":
                    if (elem.person.sex != Person.Sex.MALE)
                        needsToRemove = true;
                    break;
                case "Femmina":
                    if (elem.person.sex != Person.Sex.FEMALE)
                        needsToRemove = true;
                    break;
                case "Altro":
                    if (elem.person.sex != Person.Sex.OTHER)
                        needsToRemove = true;
                    break;
            }
            switch (filterOptions.get("date")) {
                case "Oggi":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, subtractDays(Calendar.getInstance().getTime(), 0)) != 0)
                        needsToRemove = true;
                    break;
                case "Ieri":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, subtractDays(Calendar.getInstance().getTime(), 1)) != 0)
                        needsToRemove = true;
                    break;
                case "2 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, subtractDays(Calendar.getInstance().getTime(), 2)) != 0)
                        needsToRemove = true;
                    break;
                case "3 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, subtractDays(Calendar.getInstance().getTime(), 3)) != 0)
                        needsToRemove = true;
                    break;
                case "4 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, subtractDays(Calendar.getInstance().getTime(), 4)) != 0)
                        needsToRemove = true;
                    break;
                case "5 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, subtractDays(Calendar.getInstance().getTime(), 5)) != 0)
                        needsToRemove = true;
                    break;
                case "6 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, subtractDays(Calendar.getInstance().getTime(), 6)) != 0)
                        needsToRemove = true;
                    break;
            }
            switch (filterOptions.get("hour")) {
                case "00.00 – 04.00":
                    if (!isInHourRange(elem.meeting.date, 0, 4))
                        needsToRemove = true;
                    break;
                case "04.00 – 08.00":
                    if (!isInHourRange(elem.meeting.date, 4, 8))
                        needsToRemove = true;
                    break;
                case "08.00 – 12.00":
                    if (!isInHourRange(elem.meeting.date, 8, 12))
                        needsToRemove = true;
                    break;
                case "12.00 – 16.00":
                    if (!isInHourRange(elem.meeting.date, 12, 16))
                        needsToRemove = true;
                    break;
                case "16.00 – 20.00":
                    if (!isInHourRange(elem.meeting.date, 16, 20))
                        needsToRemove = true;
                    break;
                case "20.00 – 24.00":
                    if (!isInHourRange(elem.meeting.date, 20, 24))
                        needsToRemove = true;
                    break;
            }
            switch (filterOptions.get("age")) {
                case "14 – 18":
                    if (!isInYearRange(elem.person.yearOfBirth, 14, 18))
                        needsToRemove = true;
                    break;
                case "19 – 24":
                    if (!isInYearRange(elem.person.yearOfBirth, 19, 24))
                        needsToRemove = true;
                    break;
                case "25 – 30":
                    if (!isInYearRange(elem.person.yearOfBirth, 25, 30))
                        needsToRemove = true;
                    break;
                case "31 – 40":
                    if (!isInYearRange(elem.person.yearOfBirth, 31, 40))
                        needsToRemove = true;
                    break;
                case "41 – 50":
                    if (!isInYearRange(elem.person.yearOfBirth, 41, 50))
                        needsToRemove = true;
                    break;
                case "51+":
                    if (!isInYearRange(elem.person.yearOfBirth, 51, 0))
                        needsToRemove = true;
                    break;
            }
            if(needsToRemove)
                mpIterator.remove();
        }
    }
    //funzione per eliminare un'immagine(o un file qualunque) dato il suo path 
    private static void deletePicture(String path){
        File current = new File(Uri.parse(path).getPath());
        if(current.exists()){
            current.delete();
        }
    }
    //funzione per eliminare tutti i meeting dal database più vecchi di n giorni 
    //In questa funzione vengono anche eliminate le relative foto profilo delle persone incontrate
    public static void deleteMeetingsOlderThan(int nDays, MeetingDao md, PersonDao pd){
        Date start = subtractDays(Calendar.getInstance().getTime(), nDays);
        ListenableFuture<List<MeetingPerson>> meetingsToDelete = md.getMeetingBeforeDate(start);
        Futures.addCallback(meetingsToDelete, new FutureCallback<List<MeetingPerson>>() {
            @Override
            public void onSuccess(@NullableDecl List<MeetingPerson> result) {
                for(MeetingPerson mp : result){
                    deletePicture(mp.person.profilePath);
                    pd.deleteAll(mp.person);
                    md.delete(mp.meeting);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, Executors.newSingleThreadExecutor());
    }
    //funzione per creare un nuovo file di immagine
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

}
