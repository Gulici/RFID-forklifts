using RfidFirmware;
using RfidFirmware.Configuration;
using RfidFirmware.Services;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Mocks;

var builder = Host.CreateApplicationBuilder(args);
builder.Logging.AddConsole();

builder.Services
    .AddOptions<ReaderSettings>()
    .Bind(builder.Configuration.GetSection("Reader"))
    .ValidateDataAnnotations()
    .ValidateOnStart();

var isMock = args.Contains("-mock", StringComparer.OrdinalIgnoreCase);

if (isMock)
{
    builder.Services.AddSingleton<IGpioService, MockGpioService>();
    builder.Services.AddSingleton<IRfidService, MockRfidService>();
}
else
{
    builder.Services.AddSingleton<IGpioService, GpioService>();
    builder.Services.AddSingleton<IRfidService, RfidServiceSpider>();
}

builder.Services.AddSingleton<IFileService, FileService>();
builder.Services.AddSingleton<IMainService, MainServiceOffline>();

builder.Services.AddHostedService<Worker>();

var host = builder.Build();
host.Run();
