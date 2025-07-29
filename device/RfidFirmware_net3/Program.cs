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
                    services.Configure<ReaderSettings>(hostContext.Configuration.GetSection("Reader"));
                    services.Configure<ApiSettings>(hostContext.Configuration.GetSection("Api"));

                    var flags = new AppFlags
                    {
                        IsMock = args.Contains("-mock", StringComparer.OrdinalIgnoreCase),
                        IsRegister = args.Contains("-register", StringComparer.OrdinalIgnoreCase)
                    };
                    services.AddSingleton(flags);              


                    if (flags.IsMock)
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
                    services.AddSingleton<IRegisterService, RegisterService>();

                    services.AddHttpClient<IApiService, ApiService>(client =>
                    {
                        client.BaseAddress = new Uri(hostContext.Configuration["Api:Url"]);
                        client.Timeout = TimeSpan.FromSeconds(10);
                        client.DefaultRequestHeaders.Add("Accept", "application/json");
                    });

                    services.AddHostedService<Worker>();
                });
    }
}
