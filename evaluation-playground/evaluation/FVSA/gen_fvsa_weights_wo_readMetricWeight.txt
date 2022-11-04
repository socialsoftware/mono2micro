['readMetricWeight']                            OLS Regression Results                            
==============================================================================
Dep. Variable:               combined   R-squared:                       0.005
Model:                            OLS   Adj. R-squared:                  0.005
Method:                 Least Squares   F-statistic:                     111.1
Date:                Sun, 23 Oct 2022   Prob (F-statistic):           7.58e-49
Time:                        19:09:39   Log-Likelihood:                 27359.
No. Observations:               42768   AIC:                        -5.471e+04
Df Residuals:                   42765   BIC:                        -5.469e+04
Df Model:                           2                                         
Covariance Type:            nonrobust                                         
====================================================================================
                       coef    std err          t      P>|t|      [0.025      0.975]
------------------------------------------------------------------------------------
const                0.3185      0.002    158.225      0.000       0.315       0.322
readMetricWeight     0.0001   2.02e-05      5.242      0.000    6.64e-05       0.000
n                   -0.0035      0.000    -13.896      0.000      -0.004      -0.003
==============================================================================
Omnibus:                     1648.680   Durbin-Watson:                   0.162
Prob(Omnibus):                  0.000   Jarque-Bera (JB):             1122.313
Skew:                           0.284   Prob(JB):                    1.96e-244
Kurtosis:                       2.446   Cond. No.                         199.
==============================================================================

Notes:
[1] Standard Errors assume that the covariance matrix of the errors is correctly specified.