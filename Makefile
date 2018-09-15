.PHONY: all

JARNAME=trelloApp-1.0-SNAPSHOT-jar-with-dependencies
SRCs=App TrelloAssistant
SRCdir=src/main/java/com/github/nailbiter/
KEYS=-s src/main/resources/keyring.json -m writecard
MAINCLASS=App

all: target/$(JARNAME).jar
	du -hs $<
	java -cp target/$(JARNAME).jar com.github.nailbiter.$(MAINCLASS) $(KEYS) 2>&1 | tee log/log.txt

target/$(JARNAME).jar: pom.xml \
	$(addprefix $(SRCdir),$(addsuffix .java,$(SRCs)))
	mvn package
