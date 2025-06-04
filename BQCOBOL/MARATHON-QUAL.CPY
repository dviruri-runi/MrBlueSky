      ****************************************************************  **
      * MARATHON-QUAL.CPY - Boston Marathon Qualification Standards
      *****************************************************************
       01 MARATHON-QUALIFICATION-DETAILS.
          05 MQ-MIN-AGE         PIC 999 VALUE 18.
          05 MQ-MAX-AGE         PIC 999 VALUE 99.
          05 MQ-GENDER-TYPES.
             10 MQ-MALE         PIC X VALUE 'M'.
             10 MQ-FEMALE       PIC X VALUE 'F'.
          05 MQ-QUALIFYING-INFO.
             10 MQ-QUAL-STANDARD.
                15 MQ-18-34-MALE     PIC 9(3)V99 VALUE 185.00.
                15 MQ-18-34-FEMALE   PIC 9(3)V99 VALUE 210.00.
                15 MQ-35-39-MALE     PIC 9(3)V99 VALUE 190.00.
                15 MQ-35-39-FEMALE   PIC 9(3)V99 VALUE 215.00.
                15 MQ-40-44-MALE     PIC 9(3)V99 VALUE 195.00.
                15 MQ-40-44-FEMALE   PIC 9(3)V99 VALUE 220.00.
                15 MQ-45-49-MALE     PIC 9(3)V99 VALUE 200.00.
                15 MQ-45-49-FEMALE   PIC 9(3)V99 VALUE 225.00.
                15 MQ-50-54-MALE     PIC 9(3)V99 VALUE 205.00.
                15 MQ-50-54-FEMALE   PIC 9(3)V99 VALUE 235.00.
                15 MQ-55-59-MALE     PIC 9(3)V99 VALUE 220.00.
                15 MQ-55-59-FEMALE   PIC 9(3)V99 VALUE 245.00.
                15 MQ-60-64-MALE     PIC 9(3)V99 VALUE 235.00.
                15 MQ-60-64-FEMALE   PIC 9(3)V99 VALUE 255.00.
                15 MQ-65-69-MALE     PIC 9(3)V99 VALUE 250.00.
                15 MQ-65-69-FEMALE   PIC 9(3)V99 VALUE 270.00.
                15 MQ-70-74-MALE     PIC 9(3)V99 VALUE 265.00.
                15 MQ-70-74-FEMALE   PIC 9(3)V99 VALUE 285.00.
                15 MQ-75-79-MALE     PIC 9(3)V99 VALUE 280.00.
                15 MQ-75-79-FEMALE   PIC 9(3)V99 VALUE 300.00.
                15 MQ-80-PLUS-MALE   PIC 9(3)V99 VALUE 295.00.
                15 MQ-80-PLUS-FEMALE PIC 9(3)V99 VALUE 315.00.
          05 MQ-ERROR-CODES.
             10 MQ-SUCCESS          PIC X(2) VALUE '00'.
             10 MQ-NOT-QUALIFIED    PIC X(2) VALUE '01'.
             10 MQ-ERR-MISSING-ARGS PIC X(2) VALUE 'E0'.
             10 MQ-ERR-INVALID-AGE  PIC X(2) VALUE 'E1'.
             10 MQ-ERR-INVALID-GEN  PIC X(2) VALUE 'E2'.
             10 MQ-ERR-INVALID-TIME PIC X(2) VALUE 'E3'.