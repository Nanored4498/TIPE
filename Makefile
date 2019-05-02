BUILD_FOLDER=bin
SRC_FOLDER=src
IMS=Images3 Var
RES=Traitement
DOC_FOLDER=doc

LIB=
SRC_LIST=$(SRC_FOLDER)/sources.txt

all: $(BUILD_FOLDER) $(SRC_LIST)
	javac -d $(BUILD_FOLDER) -cp "$(LIB)" @$(SRC_LIST)

help:
	# Use `make` to compile
	# `make train` to launch the interface to create and train networks
	# `make test` to launch the interface to test networks on envelope pictures

.PHONY: learning interpolation
train: $(IMS)
	java -cp "$(BUILD_FOLDER)/:$(LIB)" Interface
test: $(IMS) $(RES)
	java -cp "$(BUILD_FOLDER)/:$(LIB)" Final

$(IMS):
	mkdir Images3
	mkdir Var
	java -cp "$(BUILD_FOLDER)/:$(LIB)" ImageUtil

$(RES):
	mkdir $@

.PHONY: doc
doc:
	$(RM) -r $(DOC_FOLDER)
	mkdir $(DOC_FOLDER)
	javadoc $(SRC_FOLDER)/*.java -d $(DOC_FOLDER)

$(BUILD_FOLDER):
	mkdir $(BUILD_FOLDER)

.PHONY: $(SRC_LIST)
$(SRC_LIST):
	find $(SRC_FOLDER) -name "*.java" > $@

.PHONY: clean
clean:
	$(RM) -r $(BUILD_FOLDER) $(SRC_LIST) $(IMS) $(RES) $(DOC_FOLDER)