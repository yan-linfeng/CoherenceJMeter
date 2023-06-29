package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(name = "generate", mixinStandardHelpOptions = true, description = "Generate Json data files.")
public class TestDataGenerator implements Callable<Integer> {
    private static final Pattern REGEXP_JSON_SIZE = Pattern.compile("^([0-9]+)(MB|KB|B|)$");
    private static final String[] firstNames = new String[]{"John", "James", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Thomas", "Christopher", "Daniel", "Paul", "Mark", "Donald", "George", "Kenneth", "Steven", "Edward", "Brian", "Ronald", "Anthony", "Kevin", "Jason", "Matthew", "Gary", "Timothy", "Jose", "Larry", "Jeffrey", "Frank", "Scott", "Eric", "Stephen", "Andrew", "Raymond", "Gregory", "Joshua", "Jerry", "Dennis", "Walter", "Patrick", "Peter", "Harold", "Douglas", "Henry", "Carl", "Arthur", "Ryan", "Roger"};
    private static final String[] lastNames = new String[]{ "Smith",  "Johnson",  "Williams",  "Brown",  "Jones",  "Garcia",  "Rodriguez",  "Wilson",  "Martinez",  "Anderson",   "Taylor",  "Thomas",  "Hernandez",  "Moore",  "Martin",  "Jackson",  "Thompson",  "White",  "Lopez",  "Lee", "Gonzalez",  "Harris",  "Clark",  "Lewis",  "Robinson",  "Walker",  "Perez",  "Hall",  "Young",  "Allen",  "Sanchez",  "Wright",  "King",  "Scott",  "Green",  "Adams",  "Baker",  "Gomez",  "Nelson",  "Hill",   "Ramirez",  "Campbell",  "Mitchell",  "Roberts",  "Carter",  "Phillips",  "Evans",  "Turner",  "Torres",  "Parker",  "Collins",  "Edwards",  "Stewart",  "Flores",  "Morris",  "Nguyen",  "Murphy",  "Rivera",  "Cook",  "Rogers",  "Morgan",  "Peterson",  "Cooper",  "Reed",  "Bailey",  "Bell",  "Gutierrez",  "Kelly",  "Howard",  "Ward",  "Cox",  "Diaz",  "Richardson",  "Wood",  "Watson",  "Brooks",  "Bennett",  "Gray",  "James",  "Reyes",  "Cruz",  "Hughes",  "Price",  "Myers",  "Long",  "Foster",  "Sanders",  "Ross",  "Morales",  "Powell",  "Sullivan",  "Russell",  "Ortiz",  "Jenkins",  "Perry",  "Butler",  "Coleman",  "Simmons",  "Patterson",  "Jordan",  "Reynolds",  "Hamilton",  "Graham",  "Kim",  "Davis"};
    private static final String[] hobbies = new String[]{ "Reading", "Traveling", "Cycling", "Cooking", "Photography", "Writing", "Painting", "Movies", "Swimming", "Social networking", "Gaming", "Outdoor sports", "Music", "Gardening", "Coffee", "Films", "Hiking", "Board games", "Driving", "Shopping", "Pet care", "Learning languages", "Tennis", "Fishing", "Travel", "Dancing", "Painting   ", "Gadgets", "Cooking", "Fitness", "Yoga", "Blogging", "Stamp collecting", "Watching sports", "Finance and investment", "Card games", "Baking", "Camping", "Reading comics","Car repairing"};
    private static final String[] genders = new String[]{"Male","Female"};
    private static final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    @Option(names = {"-c", "--count"}, description = "The number of json files you want to generate.")
    private int count = 100;
    @Option(names = {"-size", "--size"}, description = "The json file size you want to generate. B, KB, MB is available to specify, the max size you can specify is 32MB.")
    private String size = "10KB";
    private int JSON_SIZE_IN_BYTES = 100;

    @Option(names = {"-start", "--start-id"}, description = "The start id you can specify for your json.")
    private int start = 1;

    @Override
    public Integer call() throws Exception {
        this.JSON_SIZE_IN_BYTES = parseJSONSize(size);
        File testdataDir = new File("./testdata");
        if(testdataDir.exists()){
            testdataDir.delete();
            new File("./testdata").mkdir();
        }

        for(int i=start;i<start+count;i++){
            String filename = String.format("%s.json",i);
            Map<String,Object> entity = new HashMap();
            entity.put("id",i);
            entity.put("name",generateName());
            entity.put("age", new Random().nextInt(60)+18);
            entity.put("gender",genders[new Random().nextInt(2)]);
            entity.put("hobby",hobbies[new Random().nextInt(hobbies.length)]);
            entity.put("comment","");
            entity.put("comment",generateComment(entity));
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File("./testdata/"+filename),entity);
        }
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new TestDataGenerator()).execute(args);
        System.exit(exitCode);
    }

    private String generateComment(Map<String,Object> entity) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        int docLengthWithoutComment =objectMapper.writeValueAsString(entity).getBytes().length;
        if(docLengthWithoutComment >= JSON_SIZE_IN_BYTES){
            return "";
        }else{
            int commentLength = JSON_SIZE_IN_BYTES - docLengthWithoutComment;
            return randomAlpha(commentLength);
        }
    }

    private static String randomAlpha(int length){
        StringBuilder stringBuilder = new StringBuilder("");
        for(int i=0;i<length;i++){
            stringBuilder.append(chars[new Random().nextInt(chars.length)]);
        }
        return stringBuilder.toString();
    }

    private static String generateName() {

        Random random = new Random();
        int firstNameIndex = random.nextInt(firstNames.length);
        int lastNameIndex = random.nextInt(lastNames.length);

        return firstNames[firstNameIndex] + " " + lastNames[lastNameIndex];
    }

    private int parseJSONSize(Object json_size) {
        if(json_size == null || !(json_size instanceof String)){
            return 0;
        }
        String jsonSize = json_size.toString().toUpperCase();
        Matcher matcher = REGEXP_JSON_SIZE.matcher(jsonSize);
        if(matcher.matches()){
            String intPart = matcher.group(1);
            String unit = matcher.group(2);
            int byteCount = Integer.parseInt(intPart);
            switch (unit){
                case "KB":
                    byteCount *= 1024;
                    break;
                case "MB":
                    byteCount *= 1024*1024;
                    break;
            }
            return byteCount;
        }else{
            return 0;
        }
    }

}