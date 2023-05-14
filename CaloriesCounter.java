import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CaloriesCounter {
    static Scanner input;
    static StepTracker stepTracker;
    
    public static void main(String[] args) {
        input = new Scanner(System.in);
        stepTracker = new StepTracker();
        new TestYear().fillYear(stepTracker.yearData);//for testing
        System.out.println("This is the console utility for tracking your steps and burnt energy");
        while(true){
            pleaseSelect();
            String s = input.nextLine();
            if( ! s.matches("^\\d+$")){
                showError();
                continue;
            }
            int select = Integer.parseInt(s);
            if(select == 0){
                System.out.println("Good bye!");
                input.close();
                break;
            }
            switch (select){
                case 1:
                    stepTracker.setSteps(postSteps());
                    break;
                case 2:
                    stepTracker.setTarget(postTarget());
                    break;
                case 3:
                    stepTracker.getStats(postMonth());
                    break;
                case 4:
                    stepTracker.showYear();
                    break;
                default:
                    showError();
            }
        }
    }

    static void pleaseSelect(){
        System.out.println("Please select what you would like to do - type a number:");
        showMenu();
    }
    
    static void showMenu(){
        System.out.println("\t1 -> Record daily steps;");
        System.out.println("\t2 -> Change daily target for steps;");
        System.out.println("\t3 -> Lookup monthly record;");
        System.out.println("\t4 -> Lookup year statistics;");
        System.out.println("\t0 -> Quit the application");
        //stepTracker.showYear(); //for testing
    }
    
    static void showError(){
        System.out.println("Wrong input. Please try again");
    }

    static int[] postSteps(){
        int day, month, steps;
        while(true){
            System.out.println("Type in the date and steps following this template: \"DD.MM N\"");
            String post = input.nextLine();
            if( ! post.matches("^\\d+\\W+\\d+\\W+\\d+$")){
                showError();
                continue;
            }
            Scanner parser = new Scanner(post);
            day = parser.nextInt();
            if(day > MonthData.DAYS_IN_MONTH && day <= 0){
                System.out.println("Wrong number for day. Try again:");
                continue;
            }
            month = parser.nextInt();
            if(month > 12 && month <= 0){
                System.out.println("Wrong number for month. Try again:");
                continue;
            }
            steps = parser.nextInt();
            parser.close();
            break;
        }
        int[] post = {day, month, steps};
        return post;
    }

    static int postMonth(){
        while(true){
            System.out.println("Type in the number of month to get statistics: N");
            String post = input.nextLine();
            if( ! post.matches("^\\d+$")){
                System.out.println("Wrong input. Try again:");
                continue;
            }
            int mon = Integer.parseInt(post);
            if(mon <= 0 && mon > 12){
                System.out.println("Wrong number for month. Try again:");
                continue;
            }
            return mon;
        }
    }

    static int postTarget()
    {
        while(true){
            System.out.println("Type in the new target (will be applied since the last record)");
            String post = input.nextLine();
            if( ! post.matches("^\\d+$")){
                System.out.println("Wrong input. Try again:");
                continue;
            }
            int trgt = Integer.parseInt(post);
            if(trgt <= StepTracker.target){
                System.out.println("New target cannot be less than the current one or the same. Try again:");
                continue;
            }
            return trgt;
        }
    }
//==========================================================================================
    static class StepTracker{
        //private int steps; 
        private static int target = 10_000;
        private HashMap<Integer, MonthData> yearData;

        StepTracker(){
            yearData = new HashMap<Integer, MonthData>();
            for(int i = 1; i <= 12; i++){
                yearData.put(i, new MonthData(i));
            }
        }

        public void setSteps(int[] post) {//передает шаги в CaloriesCounter.postSteps() 
            yearData                    
                .get(post[1])           //выбирает месяц        
                .monthStats[post[0]-1]  //выбирает день
                = post[2];              
        }

        public void getStats(int i){    //отработка п.2
            yearData.get(i).showMonth();
        }

        public int getTarget() {
            return target;
        }

        public void setTarget(int newTarget){ //отработка п.3

            int[] fromDate = getLastRecord();
            updateTargetsOnward(fromDate, newTarget);
            System.out.printf("\nSince %s the new target is set at %d\n\n", dateToString(fromDate), newTarget);
        }

        public int[] getLastRecord(){
            for(int m = 12; m > 0; m--){
                MonthData mon = yearData.get(m);
                for(int d = MonthData.DAYS_IN_MONTH-1; d >= 0; d--){
                    if ( ! mon.monthStats[d].equals(0)){
                        int[] date = {d, m};
                        return date;
                    } 
                }
            }
            int[] date = {0, 1};
            return date;
        }

        public void updateTargetsOnward(int[] date, int newTarget){
            for(int m = date[1]; m <= 12; m++){
                MonthData mon = yearData.get(m);
                for(int d = date[0]; d < MonthData.DAYS_IN_MONTH; d++){
                    mon.monthTargets[d] = newTarget;
                }
            }
            StepTracker.target = newTarget;
        }

        public String dateToString(int[] date){
            String dateString = MonthData.monthNames[date[1]-1] + ", " + (date[0] + 1);
            return dateString;
        }
        
        public void showYear(){
            for(int m = 1; m <= 12; m++){
                getStats(m);
            }
        }
    }
//==========================================================================================
    static class MonthData{
        Converter conv;
        private static String[] monthNames = {
                "January", "February", "March",
                "April", "May", "June",
                "July", "August", "September",
                "October", "November", "December"
        };
        private static final int DAYS_IN_MONTH = 10;
        private Integer[] monthStats = new Integer[DAYS_IN_MONTH];
        private Integer[] monthTargets = new Integer[DAYS_IN_MONTH];
        private String monthName;
        private int monthNumber; 

        MonthData(int monthNumber){
            this.monthNumber = monthNumber;
            this.monthName = this.getMonthName();
            Arrays.fill(this.monthStats, 0);
            Arrays.fill(this.monthTargets, StepTracker.target);
            conv = new Converter();
        }

        public String getMonthName(){
            return monthNames[monthNumber-1];
        }

        public int getMonthSteps(){
            int total = 0;
            for(int i : monthStats){
                total += i;
            }
            return total;
        }

        public void showMonth(){
            showMonthSteps();
            showMonthMax();
            showMonthAverage();
            showMonthDistance();
            showMonthEnergy();
            showBestSeries();
        }

        public void showMonthSteps(){
            System.out.printf("\nTotal of %s is %d.\n", getMonthName(), getMonthSteps());
            StringBuilder s = new StringBuilder();
            for(int d = 0; d < DAYS_IN_MONTH; d++){
                if(this.monthStats[d] > 0){
                    s.append(String.format("%d day: %d\n", d+1, this.monthStats[d]));
                }
            }
            //s.delete(s.length()-1, s.length()-1);
            System.out.println(s);
        }

        public void showMonthDistance(){
            double distance = conv.getDistance(getMonthSteps());
            System.out.printf("You have walked %,.1f km in this month\n", distance);
        }

        public void showMonthEnergy(){
            int energy = (int) conv.getEnergy(getMonthSteps());
            System.out.printf("You have burnt %d kcal in this month\n", energy);
        }     
        
        public int getAverageSteps(){
            return (int) getMonthSteps()/MonthData.DAYS_IN_MONTH;
        }

        public void showMonthAverage(){
            System.out.println("The month's average of steps is " + getAverageSteps());
        }

        public ArrayList<Integer> getMonthMax(){
            ArrayList<Integer> maxAndIndexes = new ArrayList<>(); //1st value will be max, then indexes
            int max = 1;
            for(int i = 0; i < monthStats.length; i++) {//от 0 до 29
                if(monthStats[i] > max){
                    max = monthStats[i];
                }
            }
            if(max == 1){
                maxAndIndexes.add(0);
                return maxAndIndexes;
            }
            maxAndIndexes.add(max);
            for(int i = 0; i < monthStats.length; i++) {//от 0 до 29
                if(monthStats[i] == max){
                    maxAndIndexes.add(i);
                }
            }
            return maxAndIndexes;
        }

        public void showMonthMax(){
            StringBuilder s = new StringBuilder(String.format("The maximum of %s is: ", getMonthName()));
            ArrayList<Integer> maxAndIndexes = getMonthMax();
            s.append(maxAndIndexes.get(0));
            if(maxAndIndexes.size() > 1){
                s.append(" on ");
                for(int i = 1; i < maxAndIndexes.size(); i++){
                    s.append("day " + (maxAndIndexes.get(i)+1));
                    if(i < maxAndIndexes.size()-1){
                        s.append(", ");
                    }
                }        
            }
            System.out.println(s);
        }

        public int getBestSeries(){//макс кол-во дней подряд, когда кол-во шагов в день было выше целевого
            int best = 0, thru = 0;
            for(int i = 0; i < monthStats.length; i++){
                if (monthStats[i] >= monthTargets[i]){
                    thru++;
                }
                else thru = 0;
                if(best < thru){
                    best = thru;
                }
            }
            return best;
        }

        public void showBestSeries(){
            System.out.println("Your best series has been for " + getBestSeries() + " days through\n");
        }

        static class MyList<T> extends ArrayList<T>{
            T first(){
                return this.get(0);
            }
            
            T last(){
                return this.get(this.size()-1);
            }
        }
    }
//==========================================================================================
    static class Converter{
        final double STEP_LENGTH = 0.75;
        final double KCAL_PER_STEP = 0.050;

        public double getDistance(int steps){
            return STEP_LENGTH*steps;
        }
        public double getEnergy(int steps){
            return KCAL_PER_STEP*steps;
        }
    }
//==========================================================================================
    static class TestYear{
        public void fillYear(HashMap<Integer, MonthData> year){
            for(int m = 1; m < 12; m++){ //последний месяц не заполнен
                MonthData mon = year.get(m);
                for(int d = 0; d < MonthData.DAYS_IN_MONTH; d++){
                    mon.monthStats[d] = (int) (Math.random() * 15001);
                }
            }
            MonthData mon = year.get(12);
            for(int d = 0; d < MonthData.DAYS_IN_MONTH/2; d+=3){
                mon.monthStats[d] = (int) (Math.random() * 15001);
            }
        }
    }
}
