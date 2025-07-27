using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using RfidFirmware;
using RfidFirmware.Configuration;
using RfidFirmware.Services;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Mocks;
using System;
using System.Linq;

namespace RfidFirmware_net3
{
    class Program
    {
        static void Main(string[] args)
        {
            CreateHostBuilder(args).Build().Run();
        }

        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureLogging(logging =>
                {
                    logging.ClearProviders();
                    logging.AddConsole();
                })
                .ConfigureServices((hostContext, services) =>
                {
                    // Konfiguracja ReaderSettings (bez ValidateOnStart/ValidateDataAnnotations)
                    services.Configure<ReaderSettings>(hostContext.Configuration.GetSection("Reader"));
                    
                    var isMock = args.Contains("-mock", StringComparer.OrdinalIgnoreCase);

                    if (isMock)
                    {
                        services.AddSingleton<IGpioService, MockGpioService>();
                        services.AddSingleton<IRfidService, MockRfidService>();
                    }
                    else
                    {
                        services.AddSingleton<IGpioService, GpioService>();
                        services.AddSingleton<IRfidService, RfidServiceSpider>();
                    }

                    services.AddSingleton<IFileService, FileService>();
                    services.AddSingleton<IMainService, MainServiceOffline>();

                    services.AddHostedService<Worker>();
                });
    }
}
