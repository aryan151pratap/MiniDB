package MiniDB.db;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

public class idgenerator {
	private static final Random random = new Random();

    public static String generateId() {
        long timePart = System.currentTimeMillis();
        int randPart = random.nextInt(999999);
        return Long.toHexString(timePart) + Integer.toHexString(randPart);
    }

	public static String getDate(Object _id) {
		String id = String.valueOf(_id);
        String timeHex = id.substring(0, id.length() - 5);
        long timestamp = Long.parseLong(timeHex, 16);

        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(date);
    }

	public static void main(String args[]){
		String id = generateId();
		System.out.println(id);
		System.out.println(getDate(id));
	}

}
