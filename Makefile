.PHONY: all copy interactive

JARNAME=trelloApp-1.0-SNAPSHOT-jar-with-dependencies
SRCs=App $(addprefix util/,TrelloAssistant Util)
SRCdir=src/main/java/com/github/nailbiter/
#KEYS=-s src/main/resources/keyring.json -m uploadsmalltasklist -r src/main/resources/ -i
KEYSBASIC=-s src/main/resources/keyring.json -r src/main/resources/
KEYS=$(KEYSBASIC) -m makeCardWithCheckList
MAINCLASS=App

all: target/$(JARNAME).jar
	java -cp target/$(JARNAME).jar com.github.nailbiter.$(MAINCLASS) $(KEYS) 2>&1 | tee log/log.txt
interactive: target/$(JARNAME).jar
	java -cp target/$(JARNAME).jar com.github.nailbiter.$(MAINCLASS) $(KEYSBASIC) -i 2>&1 | tee log/log.txt
copy:
	cat $(SRCdir)/TrelloAssistant.java|sed s/com.github.nailbiter/managers.habits/ > \
		../assistantbot/src/main/java/managers/habits/TrelloAssistant.java

target/$(JARNAME).jar: pom.xml \
	$(addprefix $(SRCdir),$(addsuffix .java,$(SRCs)))
	mvn package
