(set-logic SLIA)
(synth-fun f ((name String)) String
    ((Start String (ntString))
     (ntString String (name " " "+" "-" "."
(str.++ ntString ntString)
(str.replace ntString ntString ntString)
"" " " "BRD" "DRS" "LDS" "Branding" "Direct Response" "Leads" "=" "/" "in" "_" "9" "." "microsoft" "windows" "apple" "mac" "-" "1" "2" "3" "4" "5" "6" "7" "8" "0" "," "<" ">" "/n" "%" "b" "apple" "bananas" "strawberries" "oranges" "LLC" "Inc" "Corporation" "Enterprises" "Company" "(" ")" "+" "name" ","
(int.to.str ntInt)
(ite ntBool ntString ntString)
(str.substr ntString ntInt ntInt)
))
      (ntInt Int (0 1 2 3 4 5
(+ ntInt ntInt)
(- ntInt ntInt)
(str.len ntString)
-1 1 2 3 4 5 6 7 8 9 0
(str.indexof ntString ntString ntInt)
))
(ntBool Bool (true false
(= ntInt ntInt)
(str.prefixof ntString ntString)
(str.suffixof ntString ntString)
(str.contains ntString ntString)
))
))
(constraint (= (f "+106 769-858-438") "106"))
(constraint (= (f "+83 973-757-831") "83"))
(constraint (= (f "+62 647-787-775") "62"))
(constraint (= (f "+172 027-507-632") "172"))
(constraint (= (f "+72 001-050-856") "72"))
(constraint (= (f "+95 310-537-401") "95"))
(constraint (= (f "+6 775-969-238") "6"))

(check-synth)
