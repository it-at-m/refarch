<#-- Email template -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${subject}</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
        }
        .content {
            padding: 20px;
        }
    </style>
</head>
<body>
<div class="content">
    <img src="cid:logo" alt="Logo" style="width: 200px;"/>

    <h1>Hello, ${recipientName}!</h1>
    <p>This is a test mail.</p>

    <p>Best regards</p>
</div>
</body>
</html>
