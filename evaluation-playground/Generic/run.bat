@echo off
python gen_cohesion.py
python gen_coupling.py
python gen_complexity.py
python gen_combined.py

python best_gen_cohesion.py
python best_gen_coupling.py
python best_gen_complexity.py
python best_gen_combined.py
pause