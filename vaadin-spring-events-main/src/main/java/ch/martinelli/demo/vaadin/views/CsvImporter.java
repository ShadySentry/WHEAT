package ch.martinelli.demo.vaadin.views;

import ch.martinelli.demo.vaadin.data.entity.ActionDescriptions;
import ch.martinelli.demo.vaadin.data.entity.Operation;
import ch.martinelli.demo.vaadin.repository.ActionDescriptionRepository;
import ch.martinelli.demo.vaadin.repository.OperationRepository;
import com.opencsv.CSVReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class CsvImporter {
    private final int OPERATION_NUMBER = 0;
    private final int ACTION_NUMBER = 1;
    private final int NAME_RU = 2;
    private final int NAME_EN = 3;
    private final int DESCRIPTION = 4;
    private final int length = 5;

    private final OperationRepository operationRepository;

    private final ActionDescriptionRepository actionDescriptionRepository;

    Map<Integer, Operation> operations = new HashMap<>();
    Map<OperationKey, ActionDescriptions> actions = new HashMap<>();

    public CsvImporter(OperationRepository operationRepository, ActionDescriptionRepository actionDescriptionRepository) {
        this.operationRepository = operationRepository;
        this.actionDescriptionRepository = actionDescriptionRepository;
    }

    @Data
    @AllArgsConstructor
    private class OperationKey {
        Integer operation;
        Integer action;
    }


    public void importDBData() {
        int recordNumber = 0;
        int errorCount = 0;

        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/import/operations_ru.csv"))) {
            //parsing a CSV file into CSVReader class constructor
            String[] nextLine;
            //reads one line at a time
            while ((nextLine = reader.readNext()) != null) {

                recordNumber++;

                if (nextLine.length < length) {
                    System.out.println("Wrong elements number " + recordNumber);
                    errorCount++;
                    continue;
                }
                String operationNumber = nextLine[OPERATION_NUMBER];
                String actionNumber = nextLine[ACTION_NUMBER];
                String nameEn = nextLine[NAME_EN];
                String nameRu = nextLine[NAME_RU];
                String description = nextLine[DESCRIPTION];

                if (operationNumber != null && actionNumber.isEmpty()) {
                    if (operations.containsKey(Integer.valueOf(operationNumber))) {
                        System.out.println("Duplicated operation declaration. Row number " + recordNumber);
                        errorCount++;
                        continue;
                    }

                    if (nameRu.isEmpty() && nameEn.isEmpty()) {
                        System.out.println("Skipped a wrong format string. Row number " + recordNumber);
                        errorCount++;
                        continue;
                    }
                    Operation operation = Operation.builder()
                            .operationNumber(Integer.valueOf(operationNumber))
                            .operationName_en(nameEn)
                            .operationName_ru(nameRu)
                            .operationDescription(description)
                            .build();
                    operations.put(Integer.valueOf(operationNumber), operation);

                    System.out.println("loaded operation " + operation.toString());
                } else {

                    if (operationNumber.isEmpty() && actionNumber.isEmpty()) {
                        System.out.println("Skipped a wrong format string. Row number " + recordNumber);
                        errorCount++;
                        continue;
                    }
                    if (nameEn.isEmpty() && nameRu.isEmpty()) {
                        System.out.println("Skipped a wrong format string. Row number " + recordNumber);
                        errorCount++;
                        continue;
                    }

                    Operation operation = operations.get(Integer.valueOf(operationNumber));

                    if (operation == null) {
                        System.out.println("Cant find corresponding operation. Row number " + recordNumber);
                        errorCount++;
                        continue;
                    }

                    ActionDescriptions actionDescription = ActionDescriptions.builder()
                            .actionNumber(Integer.valueOf(actionNumber))
                            .actionNameRu(nameRu)
                            .getActionNameEn(nameEn)
                            .operation(operation)
                            .build();

                    OperationKey key = new OperationKey(Integer.valueOf(operationNumber), Integer.valueOf(actionNumber));
                    if (actions.containsKey(key)) {
                        System.out.println("Duplicated action declaration." + actionDescription.toString()
                                + " Row number " + recordNumber);
                        errorCount++;
                        continue;
                    }

                    actions.put(key, actionDescription);

                    System.out.println("loaded action description" + actionDescription);
                }

            }
            System.out.println("data was loaded. Total rows:" + recordNumber
                    + "\n total errors" + errorCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        saveRecords();
    }

    private void saveRecords() {
        for (Operation operation : operations.values()) {
            Operation foundOperation = operationRepository.findByOperationNumber(operation.getOperationNumber());
            if (foundOperation != null) {
                foundOperation.setOperationDescription(operation.getOperationDescription());
                foundOperation.setOperationName_en(operation.getOperationName_en());
                foundOperation.setOperationName_ru(operation.getOperationName_ru());
                foundOperation=operationRepository.save(foundOperation);
                System.out.println("updated operation with id " + foundOperation.getId().toString());
            } else {
                operationRepository.save(operation);
            }
        }

        for (ActionDescriptions action : actions.values()) {
            if(action==null){
                log.warn("found null action during import. Action was ignored");
                continue;
            }
            if(action.getOperation()==null){
                log.warn("Ignored action without operation number " + action);

                continue;
            }

            List<ActionDescriptions> foundActions = actionDescriptionRepository.findAllByActionNumber(action.getActionNumber());
            ActionDescriptions foundAction=null;

            for(ActionDescriptions actionDescription: foundActions){
                if(actionDescription.getOperation()==null){
                    continue;
                }
                if (Objects.equals(actionDescription.getOperation().getOperationNumber(), action.getOperation().getOperationNumber())){
                    foundAction=actionDescription;
                }
            }

            Operation operation = operationRepository.findByOperationNumber(action.getOperation().getOperationNumber());
            if (operation == null) {
                System.out.println("cant save ActionDescriptions. No corresponding operations was found." + action);
                continue;
            }

            if (foundAction != null) {
                foundAction.setActionNameRu(action.getActionNameRu());
                foundAction.setGetActionNameEn(action.getGetActionNameEn());
                foundAction.setOperation(operation);

                foundAction=actionDescriptionRepository.save(foundAction);
                System.out.println("updated action description with id: " + foundAction.getId().toString());
            } else {
                action.setOperation(operation);
                actionDescriptionRepository.save(action);
            }

        }
    }


}
