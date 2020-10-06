# About

CLEX is a rule- and knowledge-based system for information extraction from financial reports. 
CLEX won the PolEval 2020 [Task 4: Information extraction and entity typing from long documents with complex layouts](http://poleval.pl/tasks/task4/). 

CLEX extracts the following fields from the documents in hOCR format:
* *company* — name of the company
* *drawing_date* — date which specifies when the financial report was submitted
* *period_from* — start of the obligation period
* *period_to* — end of the obligation period
* *postal_code* — postal code of the company
* *city* — the city where the company is registered
* *street* — the name of the street where the company is registerd
* *street_no* — the number of the street at which the company is registered
* *people* — members/chairmen of the company management. A cell contains a list of tuples.

Sample output:

```
id;company;drawing_date;period_from;period_to;postal_code;city;street;street_no;people
330236;WOJAS SA;2015-12-31;2016-01-01;2016-06-30;34-400;NOWY TARG;Ludźmierska;29;[('2016-08-22', 'Kazimierz Ostatek', 'Wiceprezes Zarządu'), ('2016-08-22', 'Wiesław Wojas', 'Prezes Zarządu')]
241420;WILBO SA;2012-06-30;2013-01-01;2013-06-30;81-029;Gdynia;Przemysłowa;8;[]
174792;INSTAL KRAKÓW SA;;2011-01-01;2011-06-30;30-732;Kraków;Konstantego Brandla;1;[('', 'Rafał Markiewicz', 'Członek Zarządu'), ('', 'Rafał Rajtar', 'Członek Zarządu'), ('', 'Jan Szybiński', 'Członek Zarządu'), ('', 'Piotr Juszczyk', 'Prezes Zarządu')]
367065;AKCYJNA , RSY SA;2017-10-02;2017-01-01;2017-06-30;65-119;ZIELONA GÓRA;;;[('2017-10-02', 'Grzegorz Wrona', 'Wiceprezes Zarządu'), ('2017-10-02', 'Mariusz Matusik', 'Prezes Zarządu')]
394366;CDRL SA;2018-08-13;2018-01-01;2018-06-30;64-000;Pianowo;Kwiatowa;2;[('2018-08-13', 'Marek Dworczak', 'Prezes Zarządu'), ('2018-08-13', 'Tomasz Przybyła', 'Wiceprezes Zarządu')]
92377;MULTIMEDIA POLSKA SA;2007-12-31;2008-01-01;2008-06-30;81-341;Gdynia;Tadeusza Wendy;7/9;[('2007-12-31', 'Andrzej Rogowski', 'Prezes Zarządu')]
92126;EUROTEL SA;;2008-01-01;2008-06-30;80-286;Gdańsk;Jaśkowa Dolina;57;[('', 'Marek Parnowski', 'Członek Zarządu'), ('', 'Krzysztof Stepokura', 'Prezes Zarządu'), ('', 'Jacek Niedbalski', 'Członek Zarządu'), ('', 'Tomasz Basiński', 'Wiceprezes Zarządu')]
```

## Contributors

* Michał Marcińczuk
* Michał Olek
* Marcin Oleksy
* Jan Wieczorek

##

# Compile

```bash
./mvnw clean package
```

# Evaluation 

## Official PolEval 2020 dev dataset

Create a folder:
```
mkdir eval && cd eval
```

Download the dataset:
```
wget http://poleval.pl/task4/task4-validate.zip && unzip task4-validate.zip
```

Create an index of hocr files:
```
find reports/ -name "*.hocr" > index-hocr.list && wc -l index-hocr.list 
```

Checkout repo with evaluation scripts:
```
git clone ssh://gitolite3@poleval2020.nlp.ipipan.waw.pl:8222/poleval-financial-reports-pl
```

Generate CLEX output in the form of a CSV file:
```
cd ..
./clex extract -i eval/index-hocr.list -o eval/poleval-financial-reports-pl/dev-output.csv
```

Convert CSV file into TSV accepted by geval:
```
cd eval/poleval-financial-reports-pl
python out-csv-to-tsv.py --csv-file dev-output.csv \
                         --mapping-file dev-0/file-name-mapping.tsv \
                         --in-file dev-0/in.tsv.xz > dev-0/out.tsv
```

Download GEval scripts:
```
wget https://gonito.net/get/bin/geval
chmod u+x geval
```

Run evaluation against gold data:
```
./geval -t dev-0/
```

Expected output (formated):
```
F1(UC)  0.694±0.017
F1      0.682±0.018

                    F1           P            R
address	            0.901±0.016  0.901±0.016  0.901±0.016
person	            0.607±0.024  0.597±0.033  0.619±0.023
city                0.952±0.017  0.955±0.017  0.952±0.017
postal_code         0.907±0.024  0.942±0.019  0.876±0.029
street	            0.900±0.023  0.911±0.023  0.888±0.025
street_no           0.907±0.023  0.928±0.022  0.889±0.026
company	            0.893±0.027  0.904±0.028  0.881±0.029
drawing_date	    0.437±0.043  0.445±0.042  0.428±0.043
period_from         0.971±0.013  0.974±0.013  0.968±0.014
period_to           0.967±0.014  0.970±0.014  0.964±0.015
person_name         0.805±0.023  0.792±0.035  0.821±0.024
person_position     0.724±0.030  0.712±0.039  0.739±0.031
person_sign_date    0.427±0.042  0.425±0.046  0.432±0.043
person_name_postion 0.695±0.031  0.682±0.039  0.707±0.032
person_name_sign    0.399±0.042  0.394±0.046  0.410±0.043
```


## Custom on the PolEval 2020 train dataset

```bash
./clex eval -i LOCAL_PATH/task4-train/index-hocr.list \
            -m data/task4-train/ground_truth-train-v1.csv \
            -o report.tsv
```

Expected output:
```bash
                Type |  True | False | Accuracy
--------------------------------------------------------------------------------
                city |  1443 |   216 |    86.98%
              person |  1656 |  6450 |    20.43%
           street_no |  1405 |   254 |    84.69%
              street |  1293 |   366 |    77.94%
         period_from |  1624 |    35 |    97.89%
             company |  1213 |   446 |    73.12%
        drawing_date |   657 |  1002 |    39.60%
         postal_code |  1377 |   282 |    83.00%
           period_to |  1616 |    43 |    97.41%
--------------------------------------------------------------------------------
               TOTAL | 12284 |  9094 |    57.46%
```

# License

Copyright (C) Wrocław University of Science and Technology (PWr), 2020. All rights reserved.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.