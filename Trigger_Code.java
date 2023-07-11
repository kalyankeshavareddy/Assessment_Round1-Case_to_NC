trigger CreateNonconformance on Case (after insert, after update) {
// Collect all the Case Ids that need to be processed
Set<Id> caseIds = new Set<Id>();
for (Case caseRecord : Trigger.new) {
if (caseRecord.Type == 'Problem' &&
(Trigger.isInsert || (Trigger.isUpdate && caseRecord.Type !=
Trigger.oldMap.get(caseRecord.Id).Type))) {
caseIds.add(caseRecord.Id);
}
}
// Query existing Nonconformance records for the selected Cases
Map<Id, Nonconformance_c> existingNonconformances = new Map<Id,
Nonconformance_c>(
[SELECT Id, Case_c FROM Nonconformancec WHERE Case_c IN :caseIds]
);
List<Nonconformance_c> nonconformancesToCreate = new List<Nonconformance_c>();
for (Case caseRecord : Trigger.new) {
if (caseRecord.Type == 'Problem' &&
!existingNonconformances.containsKey(caseRecord.Id)) {
Nonconformance_c nc = new Nonconformance_c();
nc.Priority__c = caseRecord.Priority;
nc.Title__c = caseRecord.Subject;
nc.Description__c = caseRecord.Description;
nc.QMS_Reference_Number__c = caseRecord.CaseNumber;
nonconformancesToCreate.add(nc);
}
}
// Insert the newly created Nonconformance records
if (!nonconformancesToCreate.isEmpty()) {
insert nonconformancesToCreate;
// Update the Case records with the Nonconformance reference
List<Case> casesToUpdate = new List<Case>();
for (Nonconformance__c nc : nonconformancesToCreate) {
Case caseRecord = new Case(Id = nc.Case_c, SQX_NC_Reference_c = nc.Id);
casesToUpdate.add(caseRecord);
}
update casesToUpdate;
}
}