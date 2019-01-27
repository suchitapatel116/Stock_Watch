# Stock_Watch
An app monitoring stocks

- The Android application displays a sorted list of stocks that user has selected.
- The list can be updated anytime.
- Displays stock symbol, company name, current price, daily price change and percentage price change.
- The data is fetched through (http://d.yimg.com/aq/autoc) API by querying it and details about selected stock is fetch through "https://api.iextrading.com" API. The output is in JSON format.
- The selected stock data is stored in device's SQLite database.
- Uses Recycler View and SwipeRefresh functionality (to refresh the stock data).
